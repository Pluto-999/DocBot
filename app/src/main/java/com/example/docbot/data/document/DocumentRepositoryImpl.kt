package com.example.docbot.data.document

import android.net.Uri
import com.example.docbot.data.models.ProcessingStatus
import com.example.docbot.data.conversation.ConversationLocalDataSource
import com.example.docbot.data.document.processing.DocumentProcessingScheduler
import com.example.docbot.data.document.processing.DocumentProcessor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

const val MAX_DOCUMENTS = 5

class DocumentRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    private val documentProcessor: DocumentProcessor,
    private val documentProcessingScheduler: DocumentProcessingScheduler
) : DocumentRepository {

    override fun getDocumentTitles(conversationId: Long): Flow<List<String>> {
        return conversationLocalDataSource.getDocumentTitlesFromId(conversationId)
    }

    override fun getDocumentProcessingFlow(conversationId: Long): Flow<List<ProcessingStatus>> {
        return documentLocalDataSource.getProcessingFlow(conversationId)
    }

    private fun insertDocumentToProcess(
        documentName: String,
        contentsHash: String,
        conversationId: Long
    ): Long {
        val documentId = documentLocalDataSource.insertNewDocument(
            documentName,
            contentsHash,
            ProcessingStatus.PROCESSING,
            conversationId
        )
        return documentId
    }

    override fun updateProcessingStatus(documentId: Long, processingStatus: ProcessingStatus) {
        documentLocalDataSource.updateProcessingStatus(documentId, processingStatus)
    }

    // create the work request
    override suspend fun processDocument(uri: Uri, conversationId: Long): Boolean {

        val documentCount = conversationLocalDataSource.getDocumentCount(conversationId)
        if (documentCount >= MAX_DOCUMENTS) {
            return false // unsuccessful process, since we are already at max documents !
        }

        // returns false here since no text could be extracted
        val extractedData = documentProcessor.extractDocumentData(uri) ?: return false

        val documentInDb = documentLocalDataSource.findDocumentHash(extractedData.documentHash)

        // if the document is in the database, we need to link it to this conversation
        // also, need to get the processing status of it !!! -- should this now be implicit ??
        if (documentInDb) {
            documentLocalDataSource.linkDocumentToConversation(conversationId, extractedData.documentHash)
        }

        // otherwise, create the worker
        else {
            val documentId = insertDocumentToProcess(
                extractedData.documentName,
                extractedData.documentHash,
                conversationId
            )

            documentProcessingScheduler.scheduleProcessing(
                extractedData.documentHash,
                extractedData.extractedText,
                documentId,
                conversationId
            )
        }
        return true
    }

    // this function is the logic that the worker needs to carry out (i.e. processing the chunks which takes time) !!
    // (therefore, the ProcessDocumentExpeditedWorker just calls this method)
    override suspend fun processChunks(
        documentId: Long,
        documentContents: String
    ) {
        val processedChunks = documentProcessor.getProcessedChunks(documentContents)

        for (chunk in processedChunks) {
            documentChunkLocalDataSource.insertDocumentChunk(
                chunk.contents,
                chunk.embedding,
                documentId
            )
        }
    }
}