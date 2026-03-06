package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Document_
import io.objectbox.Box
import io.objectbox.kotlin.equal
import javax.inject.Inject

class DocumentLocalDataSource @Inject constructor(
    private val documentBox: Box<Document>,
    private val documentChunkBox: Box<DocumentChunk>,
    private val conversationBox: Box<Conversation>
) {

    fun getDocumentTitles(conversationId: Long) {

    }

    fun findDocumentHash(hash: String): Boolean {
        val findDocument = documentBox
            .query(Document_.contentHash equal hash)
            .build()
            .find()

        if (findDocument.isEmpty()) {
            return false
        }
        else {
            return true
        }
    }

    // to insert the document, we need the name and hash, as well as the associated conversation (conversationId)
    fun insertDocument(name: String, hash: String, conversationId: Long): Long {
        val documentToInsert = Document(name = name, contentHash = hash)
        val documentId = documentBox.put(documentToInsert)
        // the conversation "owns" the relationship between the conversation and document
        // therefore, we also need to access this
        val conversationToUpdate = conversationBox.get(conversationId)
        conversationToUpdate.documents.add(documentToInsert)
        conversationBox.put(conversationToUpdate)
        return documentId
    }
}