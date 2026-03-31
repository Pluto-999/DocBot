package com.example.docbot.data.document.processing

import android.net.Uri
import com.example.docbot.data.embedding.EmbeddingGenerator
import com.google.ai.edge.localagents.rag.models.EmbedData
import javax.inject.Inject

data class ProcessedChunk(
    val contents: String,
    val embedding: com.google.common.collect.ImmutableList<Float>
)

data class DocumentData(
    val documentName: String,
    val extractedText: String,
    val documentHash: String
)

class DocumentProcessor @Inject constructor(
    private val textChunker: TextChunker,
    private val textExtractor: TextExtractor,
    private val embeddingGenerator: EmbeddingGenerator
) {

    fun extractDocumentData(uri: Uri): DocumentData? {
        val documentName = textExtractor.getDocumentName(uri)
        val extractedText = textExtractor.extractText(uri)
        if (extractedText.isEmpty()) return null
        val documentHash = textExtractor.getDocumentHash(extractedText)
        return DocumentData(documentName, extractedText, documentHash)
    }

    suspend fun getProcessedChunks(documentContents: String): List<ProcessedChunk> {
        val chunkedText = textChunker.chunk(documentContents)
        val overlappedChunks = textChunker.addOverlap(chunkedText)
        return overlappedChunks.map { chunk ->
            val embedding = embeddingGenerator.generateEmbedding(
                chunk,
                EmbedData.TaskType.RETRIEVAL_DOCUMENT,
                false
            )
            ProcessedChunk(chunk, embedding)
        }
    }
}