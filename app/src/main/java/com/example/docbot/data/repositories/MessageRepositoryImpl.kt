package com.example.docbot.data.repositories

import com.example.docbot.data.embedding.generateEmbedding
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.sources.DocumentChunkLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.example.docbot.data.sources.MessageLocalDataSource
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.localagents.rag.models.EmbedData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    private val engine: Engine
) : MessageRepository {

    var engineInitialised = false

    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageLocalDataSource.getMessages(conversationId)
    }

    override suspend fun sendMessage(conversationId: Long, message: String):
            Flow<com.google.ai.edge.litertlm.Message>
    {
        // add user's message
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.PROMPT
        )

        // generate response from the model
        val initialisedEngine = getInitialisedEngine()
        val conversation = initialisedEngine.createConversation()

        // do the final stage of the RAG pipeline -- i.e. embed the message and get the relevant context
        val promptEmbedding = generateEmbedding(
            message,
            EmbedData.TaskType.RETRIEVAL_QUERY,
            true
        )

        val documents = documentLocalDataSource.getDocuments(conversationId)
        val documentIds = documents.map { it.id }
        val promptContext = documentChunkLocalDataSource.getRelevantChunk(documentIds, promptEmbedding)

        val contextString =
            if (promptContext.isEmpty()) {
                ""
            } else {
                "Use the following context when answering this prompt, alongside your own knowledge if required: $promptContext"
            }

        val previousPromptsString = "Consider the previous prompts and responses that have been given: "

        val fullMessage = """
            Answer the following prompt: $message 
            
            $contextString
        """.trimIndent()

        val messageFlow = conversation.sendMessageAsync(
            com.google.ai.edge.litertlm.Message.of(fullMessage)
        )

        return messageFlow
    }

    override fun saveResponse(conversationId: Long, message: String) {
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.RESPONSE
        )
    }

    private suspend fun getInitialisedEngine(): Engine {
        if (!engineInitialised) {
            engine.initialize()
            engineInitialised = true
        }
        return engine
    }
}