package com.example.docbot.data.document

import android.net.Uri
import com.example.docbot.data.models.ProcessingStatus
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun processDocument(uri: Uri, conversationId: Long): Boolean
    suspend fun processChunks(documentId: Long, documentContents: String)
    fun updateProcessingStatus(documentId: Long, processingStatus: ProcessingStatus)
    fun getDocumentTitles(conversationId: Long): Flow<List<String>>
    fun getDocumentProcessingFlow(conversationId: Long): Flow<List<ProcessingStatus>>
}