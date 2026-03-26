package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.Document_
import com.example.docbot.data.models.ProcessingStatus
import io.objectbox.Box
import io.objectbox.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentLocalDataSource @Inject constructor(
    private val documentBox: Box<Document>,
    private val conversationBox: Box<Conversation>
) {

    fun getDocumentIds(conversationId: Long): List<Long> {
        val builder = documentBox.query()

        builder.link(Document_.conversations)
            .apply(Conversation_.id.equal(conversationId))

        return builder.build().find().map { it.id }
    }

    fun findDocumentHash(hash: String): Boolean {
        return getDocumentFromHash(hash) != null
    }

    private fun getDocumentFromHash(hash: String): Document? {
        return documentBox
            .query(Document_.contentHash.equal(hash))
            .build()
            .findUnique()
    }

    // to insert the document, we need the name and hash, as well as the associated conversation (conversationId)
    fun insertNewDocument(
        name: String,
        hash: String,
        processingStatus: ProcessingStatus,
        conversationId: Long
    ): Long {
        return documentBox.store.callInTx {
            val documentToInsert = Document(name = name, contentHash = hash, processingStatus = processingStatus)
            val documentId = documentBox.put(documentToInsert)

            // the conversation "owns" the relationship between the conversation and document
            // therefore, we also need to access this
            val conversationToUpdate = conversationBox.get(conversationId)
            conversationToUpdate.documents.add(documentToInsert)
            conversationBox.put(conversationToUpdate)

            documentId // return the documentId
        }
    }

    fun updateProcessingStatus(documentId: Long, processingStatus: ProcessingStatus) {
        val document = documentBox.get(documentId)
        document.processingStatus = processingStatus
        documentBox.put(document)
    }

    fun getProcessingFlow(conversationId: Long): Flow<List<ProcessingStatus>> {
        val builder = documentBox.query()

        builder.link(Document_.conversations)
            .apply(Conversation_.id.equal(conversationId))

        return builder
            .build()
            .subscribe()
            .toFlow()
            .map { documents ->
                documents.map { document ->
                    document.conversations.reset()
                    document.processingStatus
                }
            }
    }

    fun getConversationCountForDocument(document: Document): Int {
        return document.conversations.toList().size
    }

    fun linkDocumentToConversation(conversationId: Long, hash: String) {
        val document = getDocumentFromHash(hash)
        val conversationToUpdate = conversationBox.get(conversationId)

        if (document != null) {
            conversationToUpdate.documents.add(document)
            conversationBox.put(conversationToUpdate)

            // reassign forces objectbox to observe the change and trigger subscribers for UI
            if (document.processingStatus == ProcessingStatus.PROCESSING) {
                document.processingStatus = ProcessingStatus.PROCESSING
                documentBox.put(document)
            }
        }
    }
}