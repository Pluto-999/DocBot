package com.example.docbot.data.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.docbot.data.embedding.generateEmbedding
import com.example.docbot.data.models.ProcessingStatus
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.data.sources.DocumentChunkLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.example.docbot.workers.ProcessDocumentExpeditedWorker
import com.google.ai.edge.localagents.rag.models.EmbedData
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject

const val MAX_DOCUMENTS = 5
const val CHUNK_SIZE = 512
const val CHARS_PER_TOKEN = 4
val SEPARATORS = listOf("\n\n", "\n", " ", "")

class DocumentRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    private val workManager: WorkManager,
    @ApplicationContext private val applicationContext: Context
) : DocumentRepository {

    private val contentResolver = applicationContext.contentResolver

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

        val documentName = getDocumentName(uri)

        // get the hash to be used as the uniqueWorkName
        val extractedText = extractText(uri)
        if (extractedText.isEmpty()) return false
        val contentsHash = getDocumentHash(extractedText)

        val documentInDb = documentLocalDataSource.findDocumentHash(contentsHash)

        // if the document is in the database, we need to link it to this conversation
        // also, need to get the processing status of it !!! -- should this now be implicit ??
        if (documentInDb) {
            documentLocalDataSource.linkDocumentToConversation(conversationId, contentsHash)
        }

        // otherwise, create the worker
        else {
            val documentId = insertDocumentToProcess(
                documentName,
                contentsHash,
                conversationId
            )

            val tempFile = File(applicationContext.cacheDir, "$contentsHash.txt")
            tempFile.writeText(extractedText)

            val processRequest = OneTimeWorkRequestBuilder<ProcessDocumentExpeditedWorker>()
                .setInputData(
                    workDataOf(
                        "tempFilePath" to tempFile.absolutePath,
                        "documentId" to documentId
                    )
                )
                .addTag(conversationId.toString())
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            workManager.enqueueUniqueWork(
                uniqueWorkName = contentsHash,
                existingWorkPolicy = ExistingWorkPolicy.KEEP, // keep existing work and ignore new work if duplicate
                request = processRequest
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
        val chunkedText = chunkText(documentContents, 0)
        val overlappedChunks = addOverlap(chunkedText)
        for (chunk in overlappedChunks) {
            val embedding = generateEmbedding(
                chunk,
                EmbedData.TaskType.RETRIEVAL_DOCUMENT,
                false
            )
            documentChunkLocalDataSource.insertDocumentChunk(chunk, embedding, documentId)
        }
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
            val formattedText = formatText(parsedText)
            return formattedText
        }
        catch (e: IOException) {
            Log.e("extractText", "Failed trying to strip text: $e")
            return ""
        }
        finally {
            document.close()
            pdfInputStream?.close() // maybe need this ??
        }
    }

    private fun formatText(text: String): String {
        val newlineSpacesRegex = Regex("( *\n){2,}")
        val sameSentenceRegex = Regex("""(\w) *\n *(\w)""")
        val collapseNewlinesRegex = Regex("\n{3,}")
        return text
            .replace(newlineSpacesRegex, "\n\n")
            .replace(sameSentenceRegex, "$1 $2")
            .replace(collapseNewlinesRegex, "\n\n")
    }

    private fun chunkText(text: String, splitIndex: Int): List<String> {
        // base cases -- exhausted all separators, or text is less than chunk size
        if (splitIndex >= SEPARATORS.size || estimateChunkSize(text) <= CHUNK_SIZE) {
            return listOf(text)
        }

        val finalChunks = mutableListOf<String>()

        val splitText = text.split(SEPARATORS[splitIndex]).filter { it.isNotBlank() }

        var combinedChunks = ""

        for (chunk in splitText) {
            val chunkSize = estimateChunkSize(chunk)
            val combinedChunksSize = estimateChunkSize(combinedChunks)

            // if the chunk is bigger (WITHOUT combinedChunks), of course we need split again !!
            if (chunkSize > CHUNK_SIZE) {
                if (combinedChunks.isNotEmpty()) {
                    finalChunks.add(combinedChunks)
                    combinedChunks = ""
                }
                val smallerChunks = chunkText(chunk, splitIndex + 1)
                finalChunks.addAll(smallerChunks)
                continue
            }

            // this is the case where we can add the current chunk
            if (chunkSize + combinedChunksSize <= CHUNK_SIZE) {
                combinedChunks += if (combinedChunks.isEmpty()) chunk else SEPARATORS[splitIndex] + chunk
            }
            // otherwise, if we add the current chunk, it will go over the chunk size !!
            // therefore, we MUST add combinedChunks to the final chunks
            // then we "reset" combinedChunks
            else {
                finalChunks.add(combinedChunks)
                combinedChunks = chunk
            }
        }

        if (combinedChunks.isNotEmpty()) finalChunks.add(combinedChunks)

        return finalChunks
    }

    private fun addOverlap(chunks: List<String>): List<String> {
        val overlappedChunks = mutableListOf<String>()

        for ((i, chunk) in chunks.withIndex()) {
            val chunkSize = estimateChunkSize(chunk)

            if (chunkSize >= CHUNK_SIZE * 0.2) {
                val prevChunk = if (i > 0) chunks[i - 1] else ""
                val prevChunkSize = estimateChunkSize(prevChunk)

                val overlapSize = (prevChunkSize * 0.1 * CHARS_PER_TOKEN).toInt()

                val overlap = prevChunk.takeLast(overlapSize)

                val newChunk = overlap + chunk
                overlappedChunks.add(newChunk)
            }
            else {
                overlappedChunks.add(chunk)
            }
        }

        return overlappedChunks
    }

    private fun estimateChunkSize(text: String): Int {
        return text.length / CHARS_PER_TOKEN
    }

    private fun getDocumentHash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
        val hashString = android.util.Base64.encodeToString(
            hash,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
        return hashString
    }
}