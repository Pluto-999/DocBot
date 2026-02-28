package com.example.docbot.data.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.google.ai.edge.localagents.rag.models.EmbedData
import com.google.ai.edge.localagents.rag.models.EmbeddingRequest
import com.google.ai.edge.localagents.rag.models.GemmaEmbeddingModel
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentLocalDataSource: DocumentLocalDataSource,
    @ApplicationContext private val applicationContext: Context
) : DocumentRepository {

    private val contentResolver = applicationContext.contentResolver

    override fun getAllDocumentTitles(conversationId: Long): List<String> {
        TODO("Not yet implemented")
    }

    override fun processDocument(uri: Uri, conversationId: Long): String {

        val documentName = getDocumentName(uri, conversationId)
        val extractedText = extractText(uri)
        Log.e("EXTRACTED TEXT !!!", extractedText)

        embedChunk(extractedText)

        return documentName
    }

    private fun getDocumentName(uri: Uri, conversationId: Long): String {
        var documentName = ""

        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                documentName = it.getString(columnIndex)

                Log.e("DISPLAY NAME !!!!", "Display Name: $documentName")
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

    private fun chunkText(text: String) {

    }

    private fun embedChunk(chunk: String) {
        val embeddingModel = GemmaEmbeddingModel(
            "/data/local/tmp/slm/embeddinggemma-300M_seq2048_mixed-precision.tflite",
            "/data/local/tmp/slm/sentencepiece.model",
            false
        )

        val dataToEmbed = EmbedData.create<String>(
            chunk,
            EmbedData.TaskType.RETRIEVAL_DOCUMENT,
            false
        )

        val embeddingRequest = EmbeddingRequest.create<String>(listOf(dataToEmbed))

        val embeddingsFuture = embeddingModel.getEmbeddings(embeddingRequest)

        CoroutineScope(Dispatchers.Default).launch {
            val embeddings = embeddingsFuture.await()

            Log.e("EMBEDDINGS !!!!", embeddings.toString())
        }

    }

    private fun getDocumentHash(text: String) {

    }
}