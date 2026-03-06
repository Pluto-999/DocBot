package com.example.docbot.data.sources

import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.google.common.collect.ImmutableList
import io.objectbox.Box
import javax.inject.Inject

class DocumentChunkLocalDataSource @Inject constructor(
    private val documentBox: Box<Document>,
    private val documentChunkBox: Box<DocumentChunk>
) {

    fun insertDocumentChunk(
        textChunk: String,
        embedding: ImmutableList<Float>,
        documentId: Long
    ) {
        val documentChunkToInsert = DocumentChunk(
            chunk = textChunk,
            embedding = embedding.toFloatArray()
        )
        val document = documentBox.get(documentId)
        documentChunkToInsert.document.setTarget(document)
        documentChunkBox.put(documentChunkToInsert)
    }

}