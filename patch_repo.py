import re

with open('app/src/main/java/com/example/data/repository/AppRepository.kt', 'r') as f:
    content = f.read()

new_func = """
    suspend fun insertMessageDirectly(message: com.example.data.database.CallMessageEntity) {
        callMessageDao.insertMessage(message)
    }
"""
if "insertMessageDirectly" not in content:
    content = content.replace('suspend fun addMessage(chatId: String, sender: String, text: String, emotion: String) {', new_func + '\n    suspend fun addMessage(chatId: String, sender: String, text: String, emotion: String) {')

with open('app/src/main/java/com/example/data/repository/AppRepository.kt', 'w') as f:
    f.write(content)
