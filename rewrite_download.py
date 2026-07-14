import re

with open('app/src/main/java/com/example/ui/LocalModelViewModel.kt', 'r') as f:
    content = f.read()

new_download = """
    fun downloadModel(context: Context, modelId: String, file: HfFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadProgress.value = 0f
            _downloadStatus.value = "Starting download..."
            try {
                val url = "https://huggingface.co/$modelId/resolve/main/${file.path}"
                val client = OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build()
                val request = okhttp3.Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    throw Exception("Unexpected code $response")
                }
                
                val body = response.body
                if (body == null) {
                    throw Exception("Response body is null")
                }
                
                val fileLength = body.contentLength()
                val dir = File(context.filesDir, "models")
                if (!dir.exists()) dir.mkdirs()
                
                val outputFile = File(dir, file.path.substringAfterLast("/"))
                val input = body.byteStream()
                val output = FileOutputStream(outputFile)
                val data = ByteArray(8192)
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
"""

content = re.sub(r'fun downloadModel\(context: Context, modelId: String, file: HfFile\) \{.*?(?=fun getEstimateTokenRate)', new_download.strip() + "\n\n    ", content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/LocalModelViewModel.kt', 'w') as f:
    f.write(content)
