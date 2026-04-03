package com.example.docbot.data.document

import android.net.Uri
import com.example.docbot.data.conversation.ConversationLocalDataSource
import com.example.docbot.data.document.processing.DocumentData
import com.example.docbot.data.document.processing.DocumentProcessingScheduler
import com.example.docbot.data.document.processing.DocumentProcessor
import com.example.docbot.data.document.processing.ProcessedChunk
import com.example.docbot.data.models.ProcessingStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DocumentRepositoryImplTest {

    lateinit var conversationLocalDataSourceMock: ConversationLocalDataSource
    lateinit var documentLocalDataSourceMock: DocumentLocalDataSource
    lateinit var documentChunkLocalDataSourceMock: DocumentChunkLocalDataSource
    lateinit var documentProcessorMock: DocumentProcessor
    lateinit var documentProcessingSchedulerMock: DocumentProcessingScheduler

    lateinit var repository: DocumentRepository

    @Before
    fun setUp() {
        conversationLocalDataSourceMock = mockk<ConversationLocalDataSource>()
        documentLocalDataSourceMock = mockk<DocumentLocalDataSource>()
        documentChunkLocalDataSourceMock = mockk<DocumentChunkLocalDataSource>()
        documentProcessorMock = mockk<DocumentProcessor>()
        documentProcessingSchedulerMock = mockk<DocumentProcessingScheduler>()

        repository = DocumentRepositoryImpl(
            conversationLocalDataSourceMock,
            documentLocalDataSourceMock,
            documentChunkLocalDataSourceMock,
            documentProcessorMock,
            documentProcessingSchedulerMock
        )
    }


    /****/

    @Test
    fun testGetDocumentTitlesReturnsTitlesFromDataSource() = runTest {
        val titles = listOf("title 1", "title 2")
        every { conversationLocalDataSourceMock.getDocumentTitlesFromId(1)
        } returns flowOf(titles)

        val result = repository.getDocumentTitles(1).first()

        assertEquals(2, result.size)
        assertEquals("title 1", result[0])
        assertEquals("title 2", result[1])
    }


    /****/

    @Test
    fun testGetDocumentProcessingFlowReturnsFlowFromDataSource() = runTest {
        val statusList = listOf(ProcessingStatus.PROCESSING, ProcessingStatus.COMPLETED)
        every { documentLocalDataSourceMock.getProcessingFlow(1)
        } returns flowOf(statusList)

        val result = repository.getDocumentProcessingFlow(1).first()

        assertEquals(2, result.size)
        assertEquals(ProcessingStatus.PROCESSING, result[0])
        assertEquals(ProcessingStatus.COMPLETED, result[1])
    }


    /****/

    @Test
    fun testUpdateProcessingStatusDelegatesToDataSource() {
        every {
            documentLocalDataSourceMock.updateProcessingStatus(1, ProcessingStatus.PROCESSING)
        } just runs

        repository.updateProcessingStatus(1, ProcessingStatus.PROCESSING)

        verify { documentLocalDataSourceMock.updateProcessingStatus(1, ProcessingStatus.PROCESSING) }
    }


    /****/

    @Test
    fun testProcessDocumentReturnsFalseWhenTheConversationIsAtMaxDocuments() = runTest {
        every { conversationLocalDataSourceMock.getDocumentCount(1)
        } returns MAX_DOCUMENTS

        val result = repository.processDocument(mockk(), 1)

        assertFalse(result)
    }

    @Test
    fun testProcessDocumentReturnsFalseWhenDocumentDataIsNull() = runTest {
        val uriMock = mockk<Uri>()

        every { conversationLocalDataSourceMock.getDocumentCount(1) } returns 1
        every { documentProcessorMock.extractDocumentData(uriMock) } returns null

        val result = repository.processDocument(uriMock, 1)

        assertFalse(result)
    }

    @Test
    fun testProcessDocumentLinksDocumentAndReturnsTrueWhenDocumentInDatabase() = runTest {
        val uriMock = mockk<Uri>()

        every { conversationLocalDataSourceMock.getDocumentCount(1) } returns 1
        every { documentProcessorMock.extractDocumentData(uriMock)
        } returns DocumentData("name", "text", "hash")
        every { documentLocalDataSourceMock.findDocumentHash("hash") } returns true
        every { documentLocalDataSourceMock.linkDocumentToConversation(1, "hash") } just runs

        val result = repository.processDocument(uriMock, 1)

        verify { documentLocalDataSourceMock.linkDocumentToConversation(1, "hash") }
        assertTrue(result)

    }

    @Test
    fun testProcessDocumentInsertsDocumentAndSchedulesProcessingWhenDocumentIsNotInDatabase() = runTest {
        val uriMock = mockk<Uri>()

        every { conversationLocalDataSourceMock.getDocumentCount(1) } returns 1
        every { documentProcessorMock.extractDocumentData(uriMock)
        } returns DocumentData("name", "text", "hash")
        every { documentLocalDataSourceMock.findDocumentHash("hash") } returns false
        every { documentLocalDataSourceMock.insertNewDocument(
            "name",
            "hash",
            ProcessingStatus.PROCESSING,
            1
        ) } returns 1
        every { documentProcessingSchedulerMock.scheduleProcessing(
            "hash",
            "text",
            1,
            1) } just runs

        val result = repository.processDocument(uriMock, 1)

        verify { documentLocalDataSourceMock.insertNewDocument("name", "hash", ProcessingStatus.PROCESSING, 1) }
        verify { documentProcessingSchedulerMock.scheduleProcessing("hash", "text", 1, 1) }
        assertTrue(result)
    }


    /****/

    @Test
    fun testProcessChunksInsertsEachProcessedChunkViaDataSource() = runTest {

        val embeddingOne = com.google.common.collect.ImmutableList.of(1.0f)
        val embeddingTwo = com.google.common.collect.ImmutableList.of(2.0f)
        val processedChunks = listOf(
            ProcessedChunk(contents = "chunk1", embedding = embeddingOne),
            ProcessedChunk(contents = "chunk2", embedding = embeddingTwo)
        )

        coEvery { documentProcessorMock.getProcessedChunks("contents") } returns processedChunks
        every { documentChunkLocalDataSourceMock.insertDocumentChunk(
            any(),
            any(),
            any()
        ) } just runs

        repository.processChunks(1, "contents")

        verify { documentChunkLocalDataSourceMock.insertDocumentChunk(
            "chunk1",
            embeddingOne,
            1
        ) }
        verify { documentChunkLocalDataSourceMock.insertDocumentChunk(
            "chunk2",
            embeddingTwo,
            1
        ) }
    }

    @Test
    fun testProcessChunksDoesNothingWithEmptyContents() = runTest {
        coEvery { documentProcessorMock.getProcessedChunks("contents")
        } returns listOf()

        repository.processChunks(1, "contents")

        verify(exactly = 0) { documentChunkLocalDataSourceMock.insertDocumentChunk(
            any(),
            any(),
            any())
        }
    }
}