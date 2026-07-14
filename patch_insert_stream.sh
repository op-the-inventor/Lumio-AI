cat << 'INNER_EOF' > stream_func.txt

    suspend fun streamAICompletion(
        apiKey: String,
        modelName: String,
        userMessage: String,
        history: List<CallMessageEntity>,
        systemPrompt: String
    ): kotlinx.coroutines.flow.Flow<String> = kotlinx.coroutines.flow.flow {
        val apiMessages = mutableListOf<OpenRouterMessage>()
        apiMessages.add(OpenRouterMessage(role = "system", content = systemPrompt))
        
        val recentHistory = history.takeLast(20)
        recentHistory.forEach { msg ->
            val role = if (msg.sender == "user") "user" else "assistant"
            val content = if (msg.sender == "assistant" && msg.emotionTag != "NORMAL") {
                "[TONE: ${msg.emotionTag}] ${msg.text}"
            } else {
                msg.text
            }
            apiMessages.add(OpenRouterMessage(role = role, content = content))
        }
        apiMessages.add(OpenRouterMessage(role = "user", content = userMessage))
        
        val requestBodyMap = mapOf(
            "model" to modelName,
            "messages" to apiMessages.map { mapOf("role" to it.role, "content" to it.content) },
            "stream" to true
        )
        
        val moshi = com.squareup.moshi.Moshi.Builder().build()
        val adapter = moshi.adapter(Map::class.java)
        val jsonBody = adapter.toJson(requestBodyMap)
        
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        val request = okhttp3.Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("HTTP-Referer", "https://ai.studio/build")
            .header("X-Title", "EchoCall")
            .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonBody))
            .build()
            
        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                emit("[ERROR] API returned ${response.code}")
                return@flow
            }
            
            response.body?.source()?.let { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: continue
                    if (line.startsWith("data: ") && line != "data: [DONE]") {
                        val data = line.substring(6)
                        try {
                            val chunk = moshi.adapter(Map::class.java).fromJson(data)
                            val choices = chunk?.get("choices") as? List<Map<String, Any>>
                            val delta = choices?.firstOrNull()?.get("delta") as? Map<String, Any>
                            val content = delta?.get("content") as? String
                            if (content != null) {
                                emit(content)
                            }
                        } catch (e: Exception) {
                            // parse error
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emit("[ERROR] ${e.message}")
        }
    }
INNER_EOF

# Remove the last `}` from AppRepository.kt
sed -i '$d' app/src/main/java/com/example/data/repository/AppRepository.kt

# Append stream_func.txt
cat stream_func.txt >> app/src/main/java/com/example/data/repository/AppRepository.kt

# Put the `}` back
echo "}" >> app/src/main/java/com/example/data/repository/AppRepository.kt
