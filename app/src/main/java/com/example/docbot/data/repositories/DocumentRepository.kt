package com.example.docbot.data.repositories

import android.net.Uri
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun processDocument(uri: Uri, conversationId: Long): Flow<WorkInfo?>
    suspend fun processDocumentImpl(uri: Uri, conversationId: Long): Boolean
    fun getDocumentTitles(conversationId: Long): Flow<List<String>>
}