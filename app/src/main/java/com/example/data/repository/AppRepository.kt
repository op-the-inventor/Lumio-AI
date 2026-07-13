package com.example.data.repository

import com.example.data.api.OpenRouterMessage
import com.example.data.api.OpenRouterRequest
import com.example.data.api.OpenRouterService
import com.example.data.database.CallMessageDao
import com.example.data.database.CallMessageEntity
import com.example.data.database.ChatSessionDao
import com.example.data.database.ChatSessionEntity
import com.example.data.database.SettingDao
import com.example.data.database.SettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(
    private val settingDao: SettingDao,
    private val callMessageDao: CallMessageDao,
    private val chatSessionDao: ChatSessionDao,
    private val openRouterService: OpenRouterService
) {
    // Flow of all chat sessions
    val allSessionsFlow: Flow<List<ChatSessionEntity>> = chatSessionDao.getAllSessionsFlow()

    // Flow of messages filtered by chatId
    fun getMessagesByChatIdFlow(chatId: String): Flow<List<CallMessageEntity>> {
        return callMessageDao.getMessagesByChatIdFlow(chatId)
    }

    // Flow of all messages in history
    val allMessagesFlow: Flow<List<CallMessageEntity>> = callMessageDao.getAllMessagesFlow()

    // Get settings
    val allSettingsFlow: Flow<Map<String, String>> = settingDao.getAllSettingsFlow().map { list ->
        list.associate { it.key to it.value }
    }

    suspend fun getSettingValue(key: String): String? {
        return settingDao.getSetting(key)?.value
    }

    suspend fun saveSetting(key: String, value: String) {
        settingDao.insertSetting(SettingEntity(key, value))
    }

    suspend fun createChatSession(id: String, title: String) {
        chatSessionDao.insertSession(ChatSessionEntity(id, title))
    }

    suspend fun updateSessionTitle(id: String, title: String) {
        chatSessionDao.updateSessionTitle(id, title)
    }

    suspend fun deleteSession(id: String) {
        chatSessionDao.deleteSession(id)
        callMessageDao.clearHistoryByChatId(id)
    }

    suspend fun addMessage(chatId: String, sender: String, text: String, emotion: String) {
        callMessageDao.insertMessage(
            CallMessageEntity(
                chatId = chatId,
                sender = sender,
                text = text,
                emotionTag = emotion
            )
        )
    }

    // Overloaded for backward compatibility if any callers are missed
    suspend fun addMessage(sender: String, text: String, emotion: String) {
        addMessage("default", sender, text, emotion)
    }

    suspend fun clearCallHistoryByChatId(chatId: String) {
        callMessageDao.clearHistoryByChatId(chatId)
    }

    suspend fun clearCallHistory() {
        callMessageDao.clearHistory()
    }


    // Connect to OpenRouter to get response
    suspend fun getAICompletion(
        apiKey: String,
        modelName: String,
        userMessage: String,
        history: List<CallMessageEntity>,
        systemPrompt: String
    ): Pair<String, String> { // Returns: Pair(CleanText, EmotionTag)
        // Construct standard message sequence starting with system instructions
        val apiMessages = mutableListOf<OpenRouterMessage>()
        apiMessages.add(OpenRouterMessage(role = "system", content = systemPrompt))

        // Add recent historic messages to form dialog context (limit to last 20 for prompt space)
        val recentHistory = history.takeLast(20)
        recentHistory.forEach { msg ->
            val role = if (msg.sender == "user") "user" else "assistant"
            // Include the tone tag in the prompt for Assistant messages so the AI retains tone continuity
            val content = if (msg.sender == "assistant" && msg.emotionTag != "NORMAL") {
                "[TONE: ${msg.emotionTag}] ${msg.text}"
            } else {
                msg.text
            }
            apiMessages.add(OpenRouterMessage(role = role, content = content))
        }

        // Add current user message
        apiMessages.add(OpenRouterMessage(role = "user", content = userMessage))

        val request = OpenRouterRequest(
            model = modelName,
            messages = apiMessages
        )

        val authHeader = "Bearer $apiKey"
        val response = openRouterService.getChatCompletion(
            apiKeyHeader = authHeader,
            request = request
        )

        val rawContent = response.choices.firstOrNull()?.message?.content ?: "..."
        
        // Parse the tone tag: look for "[TONE: ANGRY]", "[TONE: SAD]", "[TONE: EXCITED]", "[TONE: NORMAL]"
        var cleanText = rawContent
        var emotion = "NORMAL"

        val regex = Regex("\\[TONE:\\s*(ANGRY|SAD|EXCITED|NORMAL)\\]", RegexOption.IGNORE_CASE)
        val matchResult = regex.find(rawContent)
        if (matchResult != null) {
            emotion = matchResult.groupValues[1].uppercase()
            // Strip the [TONE: XXX] tag from the display/speakable text
            cleanText = rawContent.replace(regex, "").trim()
        }

        return Pair(cleanText, emotion)
    }
}
