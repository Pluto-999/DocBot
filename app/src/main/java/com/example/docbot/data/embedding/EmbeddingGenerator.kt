package com.example.docbot.data.embedding

import com.google.ai.edge.localagents.rag.models.EmbedData
import com.google.common.collect.ImmutableList

interface EmbeddingGenerator {

    suspend fun generateEmbedding(
        data: String,
        taskType: EmbedData.TaskType,
        isQuery: Boolean
    ): ImmutableList<Float>
}