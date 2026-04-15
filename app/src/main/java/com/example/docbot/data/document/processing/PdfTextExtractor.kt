package com.example.docbot.data.document.processing

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.inject.Inject

class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val applicationContext: Context
): TextExtractor {

    private val contentResolver = applicationContext.contentResolver

    override fun extractText(uri: Uri): String {
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
            pdfInputStream?.close()
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

    override fun getDocumentHash(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
        val hashString = Base64.encodeToString(
            hash,
            Base64.URL_SAFE or Base64.NO_WRAP
        )
        return hashString
    }

    override fun getDocumentName(uri: Uri): String {
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
}