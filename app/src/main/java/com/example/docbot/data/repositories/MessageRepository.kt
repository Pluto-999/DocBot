package com.example.docbot.data.repositories

import com.example.docbot.data.models.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(conversationId: Long): Flow<List<Message>>
    fun addMessage(conversationId: Long, message: String)
}