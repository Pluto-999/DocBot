package com.example.docbot.data

import com.example.docbot.data.embedding.EmbeddingGenerator
import com.google.ai.edge.localagents.rag.models.EmbedData
import com.google.common.collect.ImmutableList

class FakeEmbeddingGenerator: EmbeddingGenerator {

    var throwForData: String? = null

    override suspend fun generateEmbedding(
        data: String,
        taskType: EmbedData.TaskType,
        isQuery: Boolean
    ): ImmutableList<Float> {
        if (data == throwForData) throw Exception("Embedding failed")
        return ImmutableList.of(1f, 2f)
    }
}