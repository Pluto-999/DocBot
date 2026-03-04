package com.example.docbot.data.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.google.ai.edge.localagents.rag.chunking.TextChunker
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
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
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

        val extractedText = extractText(uri)

        val hashText = getDocumentHash(extractedText)
        val documentProcessed = documentLocalDataSource.findDocumentHash(hashText)

        if (!documentProcessed) {
            val chunkedText = chunkText(extractedText)
            for (chunk in chunkedText) {
                val embedding = embedChunk(chunk)
            }
        }

        val documentName = getDocumentName(uri, conversationId)
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

//    private fun chunkText(text: String): MutableList<String> {
//        // base case
//        if (getTokenLength(text) <= 512) {
//            return mutableListOf(text)
//        }
//
//        val chunks = mutableListOf<String>()
//
//        val paragraphSplit = text.split("\\n\\n")
//        for (split in paragraphSplit) {
//            chunks += chunkText(split)
//        }
//
//        val lineSplit = text.split("\\n")
//        for (split in lineSplit) {
//            chunks += chunkText(split)
//        }
//
//        val spaceSplit = text.split(" ")
//        for (split in spaceSplit) {
//            chunks += split
//        }
//
//        return chunks
//    }

//    private fun chunkText(text: String): MutableList<String> {
//        val chunks = mutableListOf<String>()
//
//        val paragraphSplit = text.split("\n\n")
//        for (split in paragraphSplit) {
//            if (getTokenLength(split) <= 512) {
//                chunks += split
//            }
//            else {
//                val lineSplit = text.split("\n")
//                for (split in lineSplit) {
//                    if (getTokenLength(split) <= 512) {
//                        chunks += split
//                    }
//                    else {
//                        val spaceSplit = text.split(" ")
//                        for (split in spaceSplit) {
//                            chunks += split
//                        }
//                    }
//                }
//            }
//        }
//
//        return chunks
//    }

    private fun chunkText(text: String): List<String> {
        return TextChunker().chunk(text, 512, 30)
    }


//    private fun getTokenLength(chunk: String): Int {
//        return chunk.length / 4
//    }

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

    private fun getDocumentHash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
        val hashString = android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        return hashString
    }
}