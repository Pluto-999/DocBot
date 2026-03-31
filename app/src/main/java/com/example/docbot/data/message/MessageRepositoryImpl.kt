package com.example.docbot.data.message

import com.example.docbot.data.document.DocumentChunkLocalDataSource
import com.example.docbot.data.document.DocumentLocalDataSource
import com.example.docbot.data.embedding.EmbeddingGenerator
import com.example.docbot.data.message.generation.MessageGenerator
import com.example.docbot.data.message.generation.PromptFormatter
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.google.ai.edge.localagents.rag.models.EmbedData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    private val embeddingGenerator: EmbeddingGenerator,
    private val messageGenerator: MessageGenerator,
    private val promptFormatter: PromptFormatter
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
        val context = getFormattedContext(message, conversationId)

        val previousMessages = getFormattedPreviousMessages(conversationId)

        val fullContext = "$context\n\n$previousMessages"

        val fullPrompt = """
            $fullContext
            
            Answer the following prompt: $message 
        """.trimIndent()

        return messageGenerator.generateResponse(fullPrompt)
    }

    private suspend fun getFormattedContext(message: String, conversationId: Long): String {
        // do the final stage of the RAG pipeline -- i.e. embed the message and get the relevant context
        val promptEmbedding = embeddingGenerator.generateEmbedding(
            message,
            EmbedData.TaskType.RETRIEVAL_QUERY,
            true
        )

        val documentIds = documentLocalDataSource.getDocumentIds(conversationId)
        val promptContext = documentChunkLocalDataSource.getRelevantChunks(documentIds, promptEmbedding)

        return promptFormatter.formatMessageContext(promptContext)
    }

    private fun getFormattedPreviousMessages(conversationId: Long): String {
        val previousMessages = messageLocalDataSource.getRecentMessages(conversationId)
        return promptFormatter.formatPreviousMessages(previousMessages)
    }

    override fun saveResponse(conversationId: Long, message: String) {
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.RESPONSE
        )
    }
}