package com.example.docbot.data.repositories

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    suspend fun processDocument(uri: Uri, conversationId: Long)
    fun getDocumentTitles(conversationId: Long): Flow<List<String>>
}