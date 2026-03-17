package com.example.docbot.data.repositories

import android.os.Debug
import android.util.Log
import com.example.docbot.data.embedding.generateEmbedding
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.sources.DocumentChunkLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.example.docbot.data.sources.MessageLocalDataSource
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.localagents.rag.models.EmbedData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    private val engine: Engine
) : MessageRepository {

    private var engineInitialised = false

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

        val initialisedEngine = getInitialisedEngine()
        val conversation = initialisedEngine.createConversation()

        val messageContexts = addMessageContexts(message, conversationId)

        val fullMessage = """
            $messageContexts
            
            Answer the following prompt: $message 
        """.trimIndent()

        val messageFlow = conversation.sendMessageAsync(
            com.google.ai.edge.litertlm.Message.of(fullMessage)
        )

        return messageFlow
            .onCompletion {
                // TESTING MEMORY USAGE OF INFERENCE !!
                val mi = Debug.MemoryInfo()
                Debug.getMemoryInfo(mi)
                Log.e("MEMORY", "Native: ${mi.nativePss} KB")

                conversation.close()
            }
    }

    private suspend fun addMessageContexts(
        prompt: String,
        conversationId: Long
    ): String {

        // do the final stage of the RAG pipeline -- i.e. embed the message and get the relevant context
        val promptEmbedding = generateEmbedding(
            prompt,
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

        val previousMessages = messageLocalDataSource.getRecentMessages(conversationId)

        val previousMessagesFormatted = mutableListOf<String>()

        previousMessages.forEach { message ->
            if (message.messageType == MessageType.PROMPT) {
                previousMessagesFormatted.add("Prompt asked by the user: ${message.contents}")
            }
            else if (message.messageType == MessageType.RESPONSE) {
                previousMessagesFormatted.add("The response given by you: ${message.contents}")
            }
        }

        val previousMessagesString =
            if (previousMessages.isEmpty()) {
                ""
            }
            else {
                """
                    Previous prompts and responses in chronological order, if relevant:
                    ${previousMessagesFormatted.joinToString("\n")}
                """.trimIndent()
            }

        return "$contextString\n\n$previousMessagesString"
    }

    override fun saveResponse(conversationId: Long, message: String) {
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.RESPONSE
        )
    }

    private fun getInitialisedEngine(): Engine {
        if (!engineInitialised) {
            engine.initialize()
            engineInitialised = true
        }
        return engine
    }
}