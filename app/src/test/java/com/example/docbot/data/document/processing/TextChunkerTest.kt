package com.example.docbot.data.document.processing

import org.junit.Assert.*
import org.junit.Test

class TextChunkerTest {

    private val chunker = TextChunker()

    @Test
    fun testChunkReturnsEmptyListWithEmptyString() {
        val result = chunker.chunk("")

        assertEquals(1, result.size)
        assertEquals("", result[0])
    }

    @Test
    fun testChunkReturnsWholeTextWhenThereAreNoPotentialSeparators() {
        val chunker = TextChunker(separators = emptyList())

        val text = "x".repeat(4000)
        val result = chunker.chunk(text)

        assertEquals(1, result.size)
        assertEquals(text, result[0])
    }

    @Test
    fun testChunkReturnsSingleChunkWithSmallText() {
        val text = "Test"
        val result = chunker.chunk(text)

        assertEquals(1, result.size)
        assertEquals(text, result[0])
    }

    @Test
    fun testChunkReturnsSingleChunkWithExactly512ChunkSize() {
        val text = "x".repeat(2048)
        val result = chunker.chunk(text)

        assertEquals(1, result.size)
        assertEquals(text, result[0])
    }

    @Test
    fun testChunkCombinesSmallChunksTogether() {
        val text = "x".repeat(20)
        val largeText = "$text\n\n$text\n\n$text\n\n$text\n\n$text\n\n$text"

        val result = chunker.chunk(largeText)

        assertEquals(1, result.size)
        assertEquals(largeText, result[0])
    }

    @Test
    fun testChunkSplitsLargeTextIntoMultipleChunks() {
        val text = "x".repeat(1000)
        val largeText = "$text\n\n$text\n\n$text"

        val result = chunker.chunk(largeText)

        assertEquals(2, result.size)
        for (chunk in result) {
            assertTrue(chunk.length / CHARS_PER_TOKEN <= CHUNK_SIZE)
        }
    }

    @Test
    fun testChunkKeepsChunksSeparateWhenEachChunkIsTooLargeToBeCombined() {
        val text = "x".repeat(2000)
        val largeText = "$text\n\n$text\n\n$text"

        val result = chunker.chunk(largeText)

        assertEquals(3, result.size)
        for (chunk in result) {
            assertTrue(chunk.length / CHARS_PER_TOKEN <= CHUNK_SIZE)
        }
    }

    @Test
    fun testChunkUsesNextSeparatorWhenFirstSeparatorDoesNotSuffice() {
        val text = "x".repeat(1500) + "\n" + "x".repeat(1500)
        val largeText = "$text\n\n$text\n\n$text"

        val result = chunker.chunk(largeText)

        assertEquals(6, result.size)
        for (chunk in result) {
            assertTrue(chunk.length / CHARS_PER_TOKEN <= CHUNK_SIZE)
        }
    }

    @Test
    fun testChunkCombinesSmallChunksBeforeSeparatingBigChunks() {
        val smallText = "x".repeat(100)
        val largeText = "x".repeat(1500) + "\n" + "x".repeat(1500)
        val finalText = "$smallText\n\n$smallText\n\n$largeText"

        val result = chunker.chunk(finalText)

        assertEquals(3, result.size)
        for (chunk in result) {
            assertTrue(chunk.length / CHARS_PER_TOKEN <= CHUNK_SIZE)
        }
    }


    /****/

    @Test
    fun testNoOverlapAddedWithSingleChunk() {
        val testChunks = listOf("Test")

        val result = chunker.addOverlap(testChunks)

        assertEquals(result[0], testChunks[0])
    }

    @Test
    fun testNoOverlapAddedWithMultipleChunksThatAreAllSmallerThan20PercentOfChunkSize() {
        val testChunks = listOf("x".repeat(50), "y".repeat(50), "z".repeat(50))

        val result = chunker.addOverlap(testChunks)

        assertEquals(result[0], testChunks[0])
        assertEquals(result[1], testChunks[1])
        assertEquals(result[2], testChunks[2])
    }

    @Test
    fun testAddOverlapPrependsPreviousChunk() {
        val testChunks = listOf("x".repeat(500), "y".repeat(500), "z".repeat(500))

        val result = chunker.addOverlap(testChunks)

        assertTrue(result[1].startsWith("x"))
        assertTrue(result[1].endsWith("y"))
        assertTrue(result[2].startsWith("y"))
        assertTrue(result[2].endsWith("z"))
    }

}