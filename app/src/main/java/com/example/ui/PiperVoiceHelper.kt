package com.example.ui

import android.content.Context
import java.io.File

object PiperVoiceHelper {
    fun getInstalledModelPath(context: Context): String? {
        val piperDir = File(context.filesDir, "piper/models")
        return piperDir.listFiles()?.firstOrNull { it.name.endsWith(".onnx") }?.absolutePath
    }
}
