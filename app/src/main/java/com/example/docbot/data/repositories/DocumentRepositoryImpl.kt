package com.example.docbot.data.repositories

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.docbot.data.embedding.generateEmbedding
import com.example.docbot.data.sources.DocumentChunkLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.google.ai.edge.localagents.rag.chunking.TextChunker
import com.google.ai.edge.localagents.rag.models.EmbedData
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentLocalDataSource: DocumentLocalDataSource,
    private val documentChunkLocalDataSource: DocumentChunkLocalDataSource,
    @ApplicationContext private val applicationContext: Context
) : DocumentRepository {

    private val contentResolver = applicationContext.contentResolver

    override fun getAllDocumentTitles(conversationId: Long): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun processDocument(uri: Uri, conversationId: Long): String {

        val documentName = getDocumentName(uri)

        val extractedText = extractText(uri)

        val hashText = getDocumentHash(extractedText)
        val documentIsProcessed = documentLocalDataSource.findDocumentHash(hashText)

        // if the document hasn't been processed, then we need to add it to the document table,
        // along with processing and adding all of its chunks to the documentChunk table !
        if (!documentIsProcessed) {

            // first, insert the document to the document table, returning the document id
            // this document id can be used for adding the chunks so we know the associated document
            val documentId = documentLocalDataSource.insertDocument(
                documentName,
                hashText,
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

        return documentName
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

    private suspend fun extractText(uri: Uri): String {
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

    private fun getDocumentHash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
        val hashString = android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
        return hashString
    }
}