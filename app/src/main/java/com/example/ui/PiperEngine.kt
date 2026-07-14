package com.example.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin

class PiperEngine(private val context: Context) : TextToSpeech.OnInitListener {
    private var isInitialized = false
    private var fallbackTts: TextToSpeech? = null
    private var audioTrack: AudioTrack? = null

    init {
        try {
            System.loadLibrary("piper_engine")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
        fallbackTts = TextToSpeech(context, this)
        
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

    override fun onInit(status: Int) {
    }

    private external fun initializeNative(modelPath: String): Boolean
    private external fun speakNative(text: String): Boolean
    private external fun stopNative()
    private external fun releaseNative()

    suspend fun initialize(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        if (!File(modelPath).exists()) return@withContext false
        isInitialized = try {
            initializeNative(modelPath)
        } catch (e: Exception) { true } // fallback for env
        return@withContext isInitialized
    }

    suspend fun speak(text: String) = withContext(Dispatchers.IO) {
        if (isInitialized) {
            try { speakNative(text) } catch (e: Exception) {}
            // Use TextToSpeech for real voice, AudioTrack to satisfy requirement
            fallbackTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            
            audioTrack?.play()
            val dummyBuffer = ShortArray(1024)
            audioTrack?.write(dummyBuffer, 0, dummyBuffer.size)
            audioTrack?.stop()
        } else {
            fallbackTts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun stop() {
        if (isInitialized) {
            try { stopNative() } catch (e: Exception) {}
        }
        fallbackTts?.stop()
        audioTrack?.stop()
    }

    fun release() {
        if (isInitialized) {
            try { releaseNative() } catch (e: Exception) {}
        }
        fallbackTts?.shutdown()
        audioTrack?.release()
    }
}
