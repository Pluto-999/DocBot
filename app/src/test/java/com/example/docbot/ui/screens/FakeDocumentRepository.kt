package com.example.docbot.ui.screens

import android.net.Uri
import com.example.docbot.data.models.ProcessingStatus
import com.example.docbot.data.document.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDocumentRepository: DocumentRepository {

    private val titles = mutableListOf<String>()
    private val titlesFlow = MutableStateFlow<List<String>>(emptyList())
    private val processingStatusFlow = MutableStateFlow<List<ProcessingStatus>>(emptyList())

    var successfulProcess = true

    override suspend fun processDocument(
        uri: Uri,
        conversationId: Long
    ): Boolean {
        return successfulProcess
    }

    override suspend fun processChunks(documentId: Long, documentContents: String) { }

    override fun updateProcessingStatus(
        documentId: Long,
        processingStatus: ProcessingStatus
    ) {
        processingStatusFlow.value = processingStatusFlow.value + processingStatus
    }

    override fun getDocumentTitles(conversationId: Long): Flow<List<String>> {
        return titlesFlow
    }

    fun addDocumentTitles(title: String) {
        titles.add(title)
        titlesFlow.value = titles.toList()
    }

    override fun getDocumentProcessingFlow(conversationId: Long): Flow<List<ProcessingStatus>> {
        return processingStatusFlow
    }
}