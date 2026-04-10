package com.example.docbot.data

import android.net.Uri
import com.example.docbot.data.document.processing.TextExtractor

class FakeTextExtractor: TextExtractor {

    var emptyExtractedText = false

    override fun extractText(uri: Uri): String {
        return if (emptyExtractedText) "" else "extracted text"
    }

    override fun getDocumentHash(text: String): String {
        return "document hash"
    }

    override fun getDocumentName(uri: Uri): String {
        return "document name"
    }
}