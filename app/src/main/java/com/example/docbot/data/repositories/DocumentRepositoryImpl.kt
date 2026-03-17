package com.example.docbot.data.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.docbot.data.embedding.generateEmbedding
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.data.sources.DocumentChunkLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.example.docbot.workers.ProcessDocumentExpeditedWorker
import com.google.ai.edge.localagents.rag.chunking.TextChunker
import com.google.ai.edge.localagents.rag.models.EmbedData
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject

const val MAX_DOCUMENTS = 5

class DocumentRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    @ApplicationContext private val applicationContext: Context
) : DocumentRepository {

    private val contentResolver = applicationContext.contentResolver

    override fun getDocumentTitles(conversationId: Long): Flow<List<String>> {
        return conversationLocalDataSource.getDocumentTitlesFromId(conversationId)
    }

    // this method creates the WorkRequest from the ProcessDocumentWorker class
    override fun processDocument(uri: Uri, conversationId: Long): Flow<WorkInfo?> {
        val processRequest = OneTimeWorkRequestBuilder<ProcessDocumentExpeditedWorker>()
            .setInputData(
                workDataOf(
                    "uri" to uri.toString(),
                    "conversationId" to conversationId
                )
            )
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .build()


        val workManager = WorkManager.getInstance(applicationContext)

        workManager.enqueueUniqueWork(
            uniqueWorkName = uri.toString(),
            existingWorkPolicy = ExistingWorkPolicy.KEEP, // keep existing work and ignore new work
            request = processRequest
        )

        val workStatus = workManager.getWorkInfoByIdFlow(processRequest.id)

        return workStatus
    }


    // this function is the logic that the ProcessDocumentWorker needs to carry out !!
    // (therefore, the ProcessDocumentWorker just calls this method)
    override suspend fun processDocumentImpl(uri: Uri, conversationId: Long): Boolean {

        // first, check if we are at max documents already (5)
        val documentCount = conversationLocalDataSource.getDocumentCount(conversationId)
        if (documentCount >= MAX_DOCUMENTS) {
            return false // unsuccessful process, since we are already at max documents !
        }

        val documentName = getDocumentName(uri)

        val extractedText = extractText(uri)

        val hashContents = getDocumentHash(extractedText)
        val documentIsProcessed = documentLocalDataSource.findDocumentHash(hashContents)

        // if the document has been processed, we still need to associate it to the conversation !!
        if (documentIsProcessed) {
            documentLocalDataSource.linkDocumentToConversation(conversationId, hashContents)
        }
        // if the document hasn't been processed, then we need to add it to the document table,
        // along with processing and adding all of its chunks to the documentChunk table !
        else {
            // first, insert the document to the document table, returning the document id
            // this document id can be used for adding the chunks so we know the associated document
            val documentId = documentLocalDataSource.insertNewDocument(
                documentName,
                hashContents,
                conversationId
            )

            val chunkedText = chunkText(extractedText)
            for (chunk in chunkedText) {
                val embedding = generateEmbedding(
                    chunk,
                    EmbedData.TaskType.RETRIEVAL_DOCUMENT,
                    false
                )
                documentChunkLocalDataSource.insertDocumentChunk(chunk, embedding, documentId)
            }
        }

        // successful process
        return true
    }

    private fun getDocumentName(uri: Uri): String {
        var documentName = ""

        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                documentName = it.getString(columnIndex)
            }
        }

        return documentName
    }

    private fun extractText(uri: Uri): String {
        val pdfInputStream = contentResolver.openInputStream(uri)

        PDFBoxResourceLoader.init(applicationContext)
        val document = PDDocument.load(pdfInputStream)

        try {
            val pdfStripper = PDFTextStripper()
            pdfStripper.startPage = 0
            pdfStripper.endPage = document.numberOfPages
            val parsedText = pdfStripper.getText(document)
            return parsedText
        }
        catch (e: IOException) {
            Log.e("extractText", "Failed trying to strip text: $e")
            return ""
        }
        finally {
            document.close()
        }
    }

    private fun chunkText(text: String): List<String> {
        return TextChunker().chunk(text, 512, 30)
    }

    private fun getDocumentHash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
        val hashString = android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        return hashString
    }
}