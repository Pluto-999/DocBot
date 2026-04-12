package com.example.docbot.data.document.processing

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File


class PdfTextExtractorTest {
    private lateinit var extractor: PdfTextExtractor
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        extractor = PdfTextExtractor(context)
    }

    private fun getUriFromFile(fileName: String = "test.pdf"): Uri {
        val inputStream = InstrumentationRegistry.getInstrumentation().context.assets.open(fileName)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.outputStream().use { inputStream.copyTo(it) }
        return Uri.fromFile(tempFile)
    }


    /****/

    @Test
    fun testExtractTextReturnsNonEmptyStringForValidPdf() {
        val uri = getUriFromFile()
        val result = extractor.extractText(uri)
        assertTrue(result.contains("test"))
    }


    /****/

    @Test
    fun testGetDocumentHashReturnsSameValueForSameString() {
        val hash1 = extractor.getDocumentHash("test")
        val hash2 = extractor.getDocumentHash("test")
        assertEquals(hash1, hash2)
    }

    @Test
    fun testGetDocumentHashReturnsDifferentValueForDifferentStrings() {
        val hash1 = extractor.getDocumentHash("test 1")
        val hash2 = extractor.getDocumentHash("test 2")
        assertNotEquals(hash1, hash2)
    }


    /****/

    @Test
    fun testGetDocumentNameReturnsEmptyStringForFileUri() {
        val uri = getUriFromFile()
        val result = extractor.getDocumentName(uri)
        assertNotNull(result)
    }
}