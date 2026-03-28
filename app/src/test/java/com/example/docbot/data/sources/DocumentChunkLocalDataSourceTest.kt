package com.example.docbot.data.sources

import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.google.common.collect.ImmutableList
import io.objectbox.Box
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DocumentChunkLocalDataSourceTest: ObjectBoxTest() {

    lateinit var localDataSource: DocumentChunkLocalDataSource

    lateinit var documentBox: Box<Document>
    lateinit var documentChunkBox: Box<DocumentChunk>

    @Before
    fun setUp() {
        documentBox = store.boxFor(Document::class.java)
        documentChunkBox = store.boxFor(DocumentChunk::class.java)

        localDataSource = DocumentChunkLocalDataSource(
            documentBox, documentChunkBox
        )
    }


    /****/
    @Test
    fun testGetRelevantChunks() {
        val documentOne = Document()
        val documentTwo = Document()
        documentBox.put(documentOne, documentTwo)

        val embeddingOne = FloatArray(768)
        val embeddingTwo = FloatArray(768)

        val chunkOne = DocumentChunk(
            chunk = "One",
            embedding = embeddingOne
        )
        val chunkTwo = DocumentChunk(
            chunk = "Two",
            embedding = embeddingTwo
        )
        documentChunkBox.put(chunkOne, chunkTwo)

        chunkOne.document.setAndPutTarget(documentOne)
        chunkTwo.document.setAndPutTarget(documentTwo)


        val result = localDataSource.getRelevantChunks(
            listOf(documentOne.id),
            ImmutableList.copyOf(embeddingOne.toList())
        )

        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("One"))
        assertFalse(result.contains("Two"))
    }


    /****/

    @Test
    fun testInsertDocumentChunk() {
        val document = Document()
        documentBox.put(document)

        localDataSource.insertDocumentChunk(
            textChunk = "A",
            embedding = ImmutableList.of(1F, 0F, 0F),
            documentId = document.id
        )

        // chunk was inserted
        assertEquals(1, documentChunkBox.count())

        val insertedChunk = documentChunkBox.all[0]

        // chunk has correct text and embedding
        assertEquals("A", insertedChunk.chunk)
        assertArrayEquals(floatArrayOf(1F, 0F, 0F), insertedChunk.embedding, 0.001F)

        // chunk is associated to the correct document
        assertEquals(document.id, insertedChunk.document.targetId)
    }

    @Test
    fun testInsertDocumentFailsWithInvalidDocumentId() {
        localDataSource.insertDocumentChunk(
            "A",
            ImmutableList.of(1F, 0F, 0F),
            999
        )

        assertTrue(documentChunkBox.isEmpty)
    }
}