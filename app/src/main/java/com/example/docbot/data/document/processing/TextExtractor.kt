package com.example.docbot.data.document.processing

import android.net.Uri


interface TextExtractor {
    fun extractText(uri: Uri): String
    fun getDocumentHash(text: String): String
    fun getDocumentName(uri: Uri): String
}