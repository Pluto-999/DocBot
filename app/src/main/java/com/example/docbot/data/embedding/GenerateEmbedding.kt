package com.example.docbot.data.embedding

import android.util.Log
import com.google.ai.edge.localagents.rag.models.EmbedData
import com.google.ai.edge.localagents.rag.models.EmbeddingRequest
import com.google.ai.edge.localagents.rag.models.GemmaEmbeddingModel
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.guava.await

suspend fun generateEmbedding(
    data: String,
    taskType: EmbedData.TaskType,
    isQuery: Boolean
): ImmutableList<Float> {
    val embeddingModel = GemmaEmbeddingModel(
        "/data/local/tmp/slm/embeddinggemma-300M_seq2048_mixed-precision.tflite",
        "/data/local/tmp/slm/sentencepiece.model",
        false
    )

    val dataToEmbed = EmbedData.create<String>(data, taskType, isQuery)

    val embeddingRequest = EmbeddingRequest.create<String>(listOf(dataToEmbed))

    val embeddingFuture = embeddingModel.getEmbeddings(embeddingRequest)

    val embedding = embeddingFuture.await()

    return embedding
}