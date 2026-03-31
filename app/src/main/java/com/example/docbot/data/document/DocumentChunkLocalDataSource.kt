package com.example.docbot.data.document

import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.DocumentChunk_
import com.google.common.collect.ImmutableList
import io.objectbox.Box
import io.objectbox.kotlin.and
import javax.inject.Inject

const val RETURNED_CHUNKS = 3

class DocumentChunkLocalDataSource @Inject constructor(
    private val documentBox: Box<Document>,
    private val documentChunkBox: Box<DocumentChunk>
) {
    fun getRelevantChunks(
        documentIds: List<Long>,
        promptEmbedding: ImmutableList<Float>
    ): List<String> {
        val query = documentChunkBox.query(
            DocumentChunk_.embedding.nearestNeighbors(
                promptEmbedding.toFloatArray(),
                20) and
                    DocumentChunk_.documentId.oneOf(documentIds.toLongArray())
        ).build()

        val results = query.findWithScores()

        return results.take(RETURNED_CHUNKS).map { it.get().chunk }
    }

    fun insertDocumentChunk(
        textChunk: String,
        embedding: ImmutableList<Float>,
        documentId: Long
    ) {
        val documentChunkToInsert = DocumentChunk(
            chunk = textChunk,
            embedding = embedding.toFloatArray()
        )
        val document = documentBox.get(documentId) ?: return
        documentChunkToInsert.document.setTarget(document)
        documentChunkBox.put(documentChunkToInsert)
    }

}