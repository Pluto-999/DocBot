package com.example.docbot.data.repositories

import android.net.Uri

interface DocumentRepository {
    fun processDocument(uri: Uri, conversationId: Long): String
    fun getAllDocumentTitles(conversationId: Long): List<String>
}