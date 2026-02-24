package com.example.docbot.data.repositories

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentLocalDataSource: DocumentLocalDataSource,
    @ApplicationContext private val applicationContext: Context
) : DocumentRepository {

    override fun processPDF(uri: Uri) {
        val contentResolver = applicationContext.contentResolver

        Log.e("EXTRACTED TEXT !!!", extractText(contentResolver, uri))
    }

    private fun extractText(
        contentResolver: ContentResolver,
        uri: Uri
    ): String {
        val pdfInputStream = contentResolver.openInputStream(uri)

        PDFBoxResourceLoader.init(applicationContext)
        val document = PDDocument.load(pdfInputStream)

        try {
            val pdfStripper = PDFTextStripper()
            pdfStripper.startPage = 0
            pdfStripper.endPage = 1
            val parsedText = pdfStripper.getText(document)
            return parsedText
        }
        catch (e: IOException) {
            Log.e("parsePDF", "Failed trying to strip text: $e")
            return ""
        }
        finally {
            document.close()
        }
    }
}