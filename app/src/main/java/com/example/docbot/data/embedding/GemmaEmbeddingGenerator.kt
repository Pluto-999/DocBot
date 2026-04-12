package com.example.docbot.data.embedding

import com.google.ai.edge.localagents.rag.models.EmbedData
import com.google.ai.edge.localagents.rag.models.EmbeddingRequest
import com.google.ai.edge.localagents.rag.models.GemmaEmbeddingModel
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.guava.await
import javax.inject.Inject

class GemmaEmbeddingGenerator @Inject constructor(
    private val embeddingModel: GemmaEmbeddingModel
): EmbeddingGenerator {

    override suspend fun generateEmbedding(
        data: String,
        taskType: EmbedData.TaskType,
        isQuery: Boolean
    ): ImmutableList<Float> {

        val dataToEmbed = EmbedData.create<String>(data, taskType, isQuery)

        val embeddingRequest = EmbeddingRequest.create<String>(listOf(dataToEmbed))

        val embeddingFuture = embeddingModel.getEmbeddings(embeddingRequest)

        val embedding = embeddingFuture.await()

        return embedding
    }
}

