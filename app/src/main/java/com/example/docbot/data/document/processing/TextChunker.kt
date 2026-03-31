package com.example.docbot.data.document.processing

import javax.inject.Inject


const val CHUNK_SIZE = 512
const val CHARS_PER_TOKEN = 4
val SEPARATORS = listOf("\n\n", "\n", " ", "")

class TextChunker @Inject constructor() {

    fun chunk(text: String): List<String> {
        return chunkText(text, 0)
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

    fun addOverlap(chunks: List<String>): List<String> {
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
}