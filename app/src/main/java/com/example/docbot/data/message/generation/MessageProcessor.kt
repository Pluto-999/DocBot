package com.example.docbot.data.message.generation

import com.example.docbot.data.embedding.EmbeddingGenerator
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.localagents.rag.models.EmbedData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageProcessor @Inject constructor(
    private val embeddingGenerator: EmbeddingGenerator,
    private val messageGenerator: MessageGenerator,
    private val promptFormatter: PromptFormatter
) {

    suspend fun generatePromptEmbedding(message: String): com.google.common.collect.ImmutableList<Float> {
        return embeddingGenerator.generateEmbedding(
            message,
            EmbedData.TaskType.RETRIEVAL_QUERY,
            true
        )
    }

    fun generateResponse(
        promptContext: List<String>,
        previousMessages: List<com.example.docbot.data.models.Message>,
        prompt: String
    ): Flow<Message> {

        val formattedContext = promptFormatter.formatMessageContext(promptContext)

        val formattedPreviousMessages = promptFormatter.formatPreviousMessages(previousMessages)

        val fullPrompt = """
            $formattedContext
            
            $formattedPreviousMessages
            
            Answer the following prompt: $prompt
        """.trimIndent()

        return messageGenerator.generateResponse(fullPrompt)
    }
}