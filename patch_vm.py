import re

with open('app/src/main/java/com/example/ui/CallViewModel.kt', 'r') as f:
    content = f.read()

funcs = """
    fun exportChatHistory(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val msgs = _messages.value
            if (msgs.isEmpty()) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "No chat history to export", android.widget.Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            try {
                val jsonArray = org.json.JSONArray()
                for (msg in msgs) {
                    val jsonObj = org.json.JSONObject()
                    jsonObj.put("sender", msg.sender)
                    jsonObj.put("text", msg.text)
                    jsonObj.put("emotion", msg.emotionTag)
                    jsonObj.put("timestamp", msg.timestamp)
                    jsonArray.put(jsonObj)
                }
                val jsonString = jsonArray.toString(4)
                val fileName = "chat_history_${System.currentTimeMillis()}.json"
                val resolver = context.contentResolver
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Exported to Downloads: $fileName", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Failed to create file", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun importChatHistory(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonString = inputStream?.bufferedReader().use { it?.readText() } ?: return@launch
                val jsonArray = org.json.JSONArray(jsonString)
                val newChatId = "imported_${System.currentTimeMillis()}"
                repository.createChatSession(newChatId, "Imported Chat")
                for (i in 0 until jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    val sender = jsonObj.optString("sender", "user")
                    val text = jsonObj.optString("text", "")
                    val emotion = jsonObj.optString("emotion", "NORMAL")
                    val timestamp = jsonObj.optLong("timestamp", System.currentTimeMillis())
                    
                    val msg = com.example.data.database.CallMessageEntity(
                        chatId = newChatId,
                        sender = sender,
                        text = text,
                        emotionTag = emotion,
                        timestamp = timestamp
                    )
                    repository.insertMessageDirectly(msg)
                }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    switchChatSession(newChatId)
                    android.widget.Toast.makeText(context, "Imported successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Import failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
"""

if "fun exportChatHistory" not in content:
    content = content.replace('fun clearHistory() {', funcs + '\n    fun clearHistory() {')

with open('app/src/main/java/com/example/ui/CallViewModel.kt', 'w') as f:
    f.write(content)
