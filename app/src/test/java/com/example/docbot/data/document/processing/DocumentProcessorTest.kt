package com.example.docbot.data.document.processing

import com.example.docbot.data.FakeEmbeddingGenerator
import com.example.docbot.data.FakeTextExtractor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DocumentProcessorTest {

    private lateinit var textChunkerMock: TextChunker
    private lateinit var fakeTextExtractor: FakeTextExtractor
    private lateinit var fakeEmbeddingGenerator: FakeEmbeddingGenerator

    private lateinit var documentProcessor: DocumentProcessor

    @Before
    fun setUp() {
        textChunkerMock = mockk<TextChunker>(relaxed = true)
        fakeTextExtractor = FakeTextExtractor()
        fakeEmbeddingGenerator = FakeEmbeddingGenerator()

        documentProcessor = DocumentProcessor(
            textChunkerMock,
            fakeTextExtractor,
            fakeEmbeddingGenerator
        )
    }


    /****/

    @Test
    fun testExtractDocumentDataReturnsDataFromTextExtractor() {
        val result = documentProcessor.extractDocumentData(mockk())
        assertEquals("document name", result!!.documentName)
        assertEquals("extracted text", result.extractedText)
        assertEquals("document hash", result.documentHash)
    }

    @Test
    fun testExtractDocumentDataReturnsNullIfNoTextIsExtracted() {
        fakeTextExtractor.emptyExtractedText = true
        val result = documentProcessor.extractDocumentData(mockk())
        assertNull(result)
    }


    /****/

    @Test
    fun testGetProcessedChunksReturnsListOfChunksAndEmbeddings() = runTest {
        val chunks = listOf("chunk1", "chunk2")
        val overlappedChunks = listOf("overlappedChunk1", "overlappedChunk2")
        every { textChunkerMock.chunk("contents") } returns chunks
        every { textChunkerMock.addOverlap(chunks) } returns overlappedChunks

        val result = documentProcessor.getProcessedChunks("contents")

        verify { textChunkerMock.chunk("contents") }
        verify { textChunkerMock.addOverlap(chunks) }

        assertEquals("overlappedChunk1", result[0].contents)
        assertEquals("overlappedChunk2", result[1].contents)

        assertEquals(listOf(1f, 2f), result[0].embedding)
        assertEquals(listOf(1f, 2f), result[1].embedding)
    }

    @Test
    fun testGetProcessedChunksSkipsFailedChunkAndProcessesRest() = runTest {
        val chunks = listOf("chunk1", "chunk2")
        val overlappedChunks = listOf("overlappedChunk1", "overlappedChunk2")
        every { textChunkerMock.chunk("contents") } returns chunks
        every { textChunkerMock.addOverlap(chunks) } returns overlappedChunks
        fakeEmbeddingGenerator.throwForData = "overlappedChunk1"

        val result = documentProcessor.getProcessedChunks("contents")

        assertEquals(1, result.size)
        assertEquals("overlappedChunk2", result[0].contents)
    }
}