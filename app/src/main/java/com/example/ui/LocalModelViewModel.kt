package com.example.ui

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.HfFile
import com.example.data.api.HfModelInfo
import com.example.data.api.HuggingFaceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class LocalModelViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://huggingface.co/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(HuggingFaceApi::class.java)

    private val _searchResults = MutableStateFlow<List<HfModelInfo>>(emptyList())
    val searchResults: StateFlow<List<HfModelInfo>> = _searchResults.asStateFlow()

    private val _modelFiles = MutableStateFlow<List<HfFile>>(emptyList())
    val modelFiles: StateFlow<List<HfFile>> = _modelFiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _downloadStatus = MutableStateFlow<String>("")
    val downloadStatus: StateFlow<String> = _downloadStatus.asStateFlow()

    fun searchModels(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = api.searchModels(query)
                _searchResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadModelFiles(modelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val files = api.getModelFiles(modelId).filter { it.path.endsWith(".gguf") }
                _modelFiles.value = files
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadModel(context: Context, modelId: String, file: HfFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgress.value = 0f
            _downloadStatus.value = "Starting download..."
            try {
                val url = "https://huggingface.co/$modelId/resolve/main/${file.path}"
                val connection = URL(url).openConnection()
                connection.connect()
                val fileLength = connection.contentLength

                val dir = File(context.filesDir, "models")
                if (!dir.exists()) dir.mkdirs()
                val outputFile = File(dir, file.path.substringAfterLast("/"))

                val input = connection.getInputStream()
                val output = FileOutputStream(outputFile)

                val data = ByteArray(4096)
                var total: Long = 0
                var count: Int
                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    if (fileLength > 0) {
                        _downloadProgress.value = (total * 100 / fileLength).toFloat() / 100f
                        _downloadStatus.value = "Downloading... ${total / (1024 * 1024)}MB / ${fileLength / (1024 * 1024)}MB"
                    }
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()
                _downloadStatus.value = "Download complete: ${outputFile.absolutePath}"
                _downloadProgress.value = 1f
            } catch (e: Exception) {
                e.printStackTrace()
                _downloadStatus.value = "Download failed: ${e.message}"
                _downloadProgress.value = null
            }
        }
    }

    fun getEstimateTokenRate(context: Context, modelSizeMb: Long): String {
        val ram = getTotalRAM(context)
        return if (modelSizeMb < 2000) {
            "~15-20 tok/s"
        } else if (modelSizeMb < 4000) {
            "~8-12 tok/s (Recommended)"
        } else if (modelSizeMb < 8000 && ram >= 8) {
            "~3-5 tok/s (Slow)"
        } else if (ram < 8) {
            "Not enough RAM"
        } else {
            "~1-2 tok/s (Very Slow)"
        }
    }

    private fun getTotalRAM(context: Context): Long {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return memInfo.totalMem / (1024 * 1024 * 1024)
    }
}
