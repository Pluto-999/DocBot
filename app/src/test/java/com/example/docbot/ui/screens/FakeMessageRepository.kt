package com.example.docbot.ui.screens

import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.repositories.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeMessageRepository: MessageRepository {

    private val messages = mutableListOf<Message>()
    private val messagesFlow = MutableStateFlow<List<Message>>(emptyList())

    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messagesFlow
    }

    override fun createPrompt(conversationId: Long, message: String) {
        val message = Message(contents = message, messageType = MessageType.PROMPT)
        messages.add(message)
        messagesFlow.value = messages.toList()
    }

    override suspend fun generateResponse(
        conversationId: Long,
        message: String
    ): Flow<com.google.ai.edge.litertlm.Message> {
        return flowOf(com.google.ai.edge.litertlm.Message.of("Fake response"))
    }

    override fun saveResponse(conversationId: Long, message: String) {
        val message = Message(contents = message, messageType = MessageType.RESPONSE)
        messages.add(message)
        messagesFlow.value = messages.toList()
    }
}