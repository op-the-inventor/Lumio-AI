package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_messages")
data class CallMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String = "default",
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "user" or "assistant"
    val text: String,
    val emotionTag: String // "NORMAL", "ANGRY", "SAD", "EXCITED"
)

