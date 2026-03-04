package com.example.docbot.data.sources

import javax.inject.Inject

class DocumentLocalDataSource @Inject constructor() {

    fun getDocumentTitles(conversationId: Long) {

    }

    fun findDocumentHash(hash: String): Boolean {
        return true
    }
}