package com.example.docbot.data.repositories

import com.example.docbot.data.models.Message
import com.example.docbot.data.sources.MessageLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource
) : MessageRepository {
    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageLocalDataSource.getMessages(conversationId)
    }

    override fun addMessage(conversationId: Long, message: String) {
        messageLocalDataSource.addMessage(conversationId, message)
    }

}