package com.example.docbot.data.repositories

import com.example.docbot.data.models.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(conversationId: Long): Flow<List<Message>>
    fun createPrompt(conversationId: Long, message: String)
    suspend fun generateResponse(conversationId: Long, message: String): Flow<com.google.ai.edge.litertlm.Message>
    fun saveResponse(conversationId: Long, message: String)
}