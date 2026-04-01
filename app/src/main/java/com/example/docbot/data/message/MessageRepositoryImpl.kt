package com.example.docbot.data.message

import com.example.docbot.data.document.DocumentChunkLocalDataSource
import com.example.docbot.data.document.DocumentLocalDataSource
import com.example.docbot.data.message.generation.MessageProcessor
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    private val messageProcessor: MessageProcessor
) : MessageRepository {

    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageLocalDataSource.getMessages(conversationId)
    }

    override fun createPrompt(conversationId: Long, message: String) {
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.PROMPT
        )
    }

    override suspend fun generateResponse(conversationId: Long, message: String):
            Flow<com.google.ai.edge.litertlm.Message>
    {
        val promptEmbedding = messageProcessor.generatePromptEmbedding(message)

        val documentIds = documentLocalDataSource.getDocumentIds(conversationId)
        val promptContext = documentChunkLocalDataSource.getRelevantChunks(documentIds, promptEmbedding)

        val previousMessages = messageLocalDataSource.getRecentMessages(conversationId)

        return messageProcessor.generateResponse(
            promptContext,
            previousMessages,
            message
        )
    }

    override fun saveResponse(conversationId: Long, message: String) {
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.RESPONSE
        )
    }
}