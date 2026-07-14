package com.example.ui

import android.content.Context
import android.os.Build
import android.app.ActivityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@Serializable
data class PiperVoiceLanguage(
    val code: String,
    val name_english: String
)

@Serializable
data class PiperVoiceFile(
    val size_bytes: Long,
    val md5_digest: String
)

@Serializable
data class PiperVoiceInfo(
    val key: String,
    val name: String,
    val language: PiperVoiceLanguage,
    val quality: String,
    val files: Map<String, PiperVoiceFile>
)

data class EnhancedVoiceInfo(
    val info: PiperVoiceInfo,
    val isInstalled: Boolean,
    val isDownloading: Boolean,
    val downloadProgress: Float,
    val isRecommended: Boolean,
    val primaryFileUrl: String,
    val sizeMb: Long
)

class PiperVoiceViewModel : ViewModel() {
    private val _voices = MutableStateFlow<List<EnhancedVoiceInfo>>(emptyList())
    val voices: StateFlow<List<EnhancedVoiceInfo>> = _voices

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient()

    fun loadVoices(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://huggingface.co/rhasspy/piper-voices/resolve/main/voices.json")
                    .build()
                val response = httpClient.newCall(request).execute()
                val bodyStr = response.body?.string() ?: return@launch

                // Parsing the map
                val mapType = json.decodeFromString<Map<String, PiperVoiceInfo>>(bodyStr)
                
                val piperDir = File(context.filesDir, "piper/models")
                if (!piperDir.exists()) piperDir.mkdirs()
                
                val installedFiles = piperDir.listFiles()?.map { it.name } ?: emptyList()

                val totalRam = getTotalRAM(context)
                val isHighEnd = totalRam >= 6000

                val enhancedList = mapType.values.map { info ->
                    val onnxFileEntry = info.files.entries.find { it.key.endsWith(".onnx") }
                    val sizeMb = (onnxFileEntry?.value?.size_bytes ?: 0) / (1024 * 1024)
                    
                    // Simple recommendation logic based on RAM and quality
                    val recommended = if (isHighEnd) info.quality == "high" || info.quality == "medium"
                                      else info.quality == "low" || info.quality == "medium"
                    
                    val isInstalled = installedFiles.any { it == "${info.key}.onnx" }

                    EnhancedVoiceInfo(
                        info = info,
                        isInstalled = isInstalled,
                        isDownloading = false,
                        downloadProgress = 0f,
                        isRecommended = recommended && info.language.code.startsWith("en"), // Example constraint
                        primaryFileUrl = onnxFileEntry?.key ?: "",
                        sizeMb = sizeMb
                    )
                }.sortedBy { !it.isRecommended }

                _voices.value = enhancedList
            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Failed to load voices: ${e.message}"
            }
        }
    }

    fun downloadVoice(context: Context, voice: EnhancedVoiceInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            updateVoiceState(voice.info.key) { it.copy(isDownloading = true, downloadProgress = 0f) }
            try {
                val url = "https://huggingface.co/rhasspy/piper-voices/resolve/main/${voice.primaryFileUrl}"
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                
                if (!response.isSuccessful) throw Exception("Unexpected code $response")
                val body = response.body ?: throw Exception("Null body")
                
                val piperDir = File(context.filesDir, "piper/models")
                if (!piperDir.exists()) piperDir.mkdirs()
                
                val outputFile = File(piperDir, "${voice.info.key}.onnx")
                val input = body.byteStream()
                val output = FileOutputStream(outputFile)
                val totalBytes = body.contentLength()
                
                val buffer = ByteArray(8192)
                var read: Int
                var downloaded = 0L
                var lastProgress = 0f
                
                input.use { inp ->
                    output.use { out ->
                        val md = MessageDigest.getInstance("MD5")
                        while (inp.read(buffer).also { read = it } != -1) {
                            out.write(buffer, 0, read)
                            md.update(buffer, 0, read)
                            downloaded += read
                            if (totalBytes > 0) {
                                val progress = downloaded.toFloat() / totalBytes
                                if (progress - lastProgress > 0.05f) {
                                    lastProgress = progress
                                    updateVoiceState(voice.info.key) { it.copy(downloadProgress = progress) }
                                }
                            }
                        }
                        
                        val computedMd5 = md.digest().joinToString("") { "%02x".format(it) }
                        val expectedMd5 = voice.info.files[voice.primaryFileUrl]?.md5_digest
                        if (expectedMd5 != null && computedMd5 != expectedMd5) {
                            outputFile.delete()
                            throw Exception("Checksum mismatch. Expected: $expectedMd5, Got: $computedMd5")
                        }
                    }
                }
                
                // Also download the JSON config
                val configUrl = "$url.json"
                val configReq = Request.Builder().url(configUrl).build()
                val configRes = httpClient.newCall(configReq).execute()
                if (configRes.isSuccessful) {
                    val configBody = configRes.body?.string()
                    if (configBody != null) {
                        File(piperDir, "${voice.info.key}.onnx.json").writeText(configBody)
                    }
                }
                
                updateVoiceState(voice.info.key) { it.copy(isDownloading = false, isInstalled = true, downloadProgress = 1f) }
                _statusMessage.value = "Downloaded ${voice.info.name}"
            } catch (e: Exception) {
                e.printStackTrace()
                updateVoiceState(voice.info.key) { it.copy(isDownloading = false, downloadProgress = 0f) }
                _statusMessage.value = "Download failed: ${e.message}"
            }
        }
    }

    fun deleteVoice(context: Context, voice: EnhancedVoiceInfo) {
        val piperDir = File(context.filesDir, "piper/models")
        File(piperDir, "${voice.info.key}.onnx").delete()
        File(piperDir, "${voice.info.key}.onnx.json").delete()
        updateVoiceState(voice.info.key) { it.copy(isInstalled = false) }
        _statusMessage.value = "Deleted ${voice.info.name}"
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    private fun updateVoiceState(key: String, update: (EnhancedVoiceInfo) -> EnhancedVoiceInfo) {
        _voices.value = _voices.value.map {
            if (it.info.key == key) update(it) else it
        }
    }

    fun getDevicePerformanceEstimate(context: Context): String {
        val ramMb = getTotalRAM(context)
        val cores = Runtime.getRuntime().availableProcessors()
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
        
        var rating = "★★★☆☆"
        var speed = "1.5x real time"
        var recommendation = "Medium quality voices"
        
        if (ramMb >= 6000 && cores >= 8) {
            rating = "★★★★★ Excellent"
            speed = "≈ 3.0x faster than real time"
            recommendation = "High quality voices"
        } else if (ramMb < 3000) {
            rating = "★★☆☆☆ Basic"
            speed = "≈ 0.8x real time"
            recommendation = "Low quality voices"
        }
        
        return "Device Rating:\n$rating\n\nEst. Synthesis Speed:\n$speed\n\nRecommended:\n$recommendation\n\nSpecs:\nRAM: ${ramMb}MB | Cores: $cores | ABI: $arch"
    }

    private fun getTotalRAM(context: Context): Long {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024 * 1024)
    }
}
