package com.example.docbot.data.conversation

import com.example.docbot.data.document.DocumentLocalDataSource
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Document
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
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

class ConversationRepositoryImplTest {

    lateinit var conversationLocalDataSourceMock: ConversationLocalDataSource
    lateinit var documentLocalDataSourceMock: DocumentLocalDataSource
    lateinit var conversationWorkSchedulerMock: ConversationWorkScheduler

    lateinit var repository: ConversationRepositoryImpl

    @Before
    fun setUp() {
        conversationLocalDataSourceMock = mockk<ConversationLocalDataSource>()
        documentLocalDataSourceMock = mockk<DocumentLocalDataSource>()
        conversationWorkSchedulerMock = mockk<ConversationWorkScheduler>()

        repository = ConversationRepositoryImpl(
            conversationLocalDataSourceMock,
            documentLocalDataSourceMock,
            conversationWorkSchedulerMock
        )
    }


    /****/

    @Test
    fun testCreateConversationReturnsIdFromLocalDataSource() {
        every { conversationLocalDataSourceMock.insertConversation() } returns 1
        val conversationId = repository.createConversation()
        assertEquals(1, conversationId)
    }


    /****/

    @Test
    fun testDeleteOldConversationsIsPerformedOnAllOldConversations() {
        every { conversationLocalDataSourceMock.getOldConversationsId() } returns listOf(1, 2, 3)
        every { conversationLocalDataSourceMock.getDocuments(any()) } returns emptyList()
        every { conversationLocalDataSourceMock.deleteConversation(any()) } just runs

        repository.deleteOldConversations()

        verify { conversationLocalDataSourceMock.deleteConversation(1) }
        verify { conversationLocalDataSourceMock.deleteConversation(2) }
        verify { conversationLocalDataSourceMock.deleteConversation(3) }
    }

    @Test
    fun testDeleteOldConversationsDoesNothingWhenNoOldConversations() {
        every { conversationLocalDataSourceMock.getOldConversationsId() } returns emptyList()

        repository.deleteOldConversations()

        verify(exactly = 0) { conversationLocalDataSourceMock.deleteConversation(any()) }
    }


    /****/

    @Test
    fun testDeleteConversationCancelsWorkerWhenDocumentHasOneAssociatedConversation() {
        val document = Document(contentHash = "hash")

        every { conversationLocalDataSourceMock.getDocuments(1) } returns listOf(document)
        every { documentLocalDataSourceMock.getConversationCountForDocument(document) } returns 1
        every { conversationWorkSchedulerMock.cancelDocumentProcessing("hash") } just runs
        every { conversationLocalDataSourceMock.deleteConversation(1) } just runs

        repository.deleteConversationManually(1)

        verify { conversationWorkSchedulerMock.cancelDocumentProcessing("hash") }
        verify { conversationLocalDataSourceMock.deleteConversation(1) }
    }

    @Test
    fun testDeleteConversationDoesNotCancelWorkerWhenDocumentHasMoreThanOneAssociatedConversation() {
        val document = Document(contentHash = "hash")

        every { conversationLocalDataSourceMock.getDocuments(1) } returns listOf(document)
        every { documentLocalDataSourceMock.getConversationCountForDocument(document) } returns 3
        every { conversationLocalDataSourceMock.deleteConversation(1) } just runs

        repository.deleteConversationManually(1)

        verify(exactly = 0) { conversationWorkSchedulerMock.cancelDocumentProcessing(any()) }
        verify { conversationLocalDataSourceMock.deleteConversation(1) }
    }


    /****/

    @Test
    fun testGetConversationsReturnsFlowFromDataSource() = runTest {
        val conversations = listOf(Conversation(id = 1, title = "Test"))
        every {
            conversationLocalDataSourceMock.getConversations(
                ConversationOrder.DATE_DESC,
                ConversationFilter.NONE,
                ""
            )
        } returns flowOf(conversations)

        val result = repository.getConversations(
            ConversationOrder.DATE_DESC,
            ConversationFilter.NONE,
            ""
        ).first()

        assertEquals(1, result.size)
        assertEquals("Test", result[0].title)
    }


    /****/

    @Test
    fun testUpdateTitleDelegatesToDataSource() {
        every { conversationLocalDataSourceMock.updateConversationTitle(1, "Test") } just runs

        repository.updateTitle(1, "Test")

        verify { conversationLocalDataSourceMock.updateConversationTitle(1, "Test") }
    }


    /****/

    @Test
    fun testToggleFavouriteRemovesFavouriteIfConversationIsCurrentlyFavourite() {
        every { conversationLocalDataSourceMock.removeConversationFromFavourites(1) } just runs

        val successfulToggle = repository.toggleFavourite(1, true)

        verify { conversationLocalDataSourceMock.removeConversationFromFavourites(1) }
        assertTrue(successfulToggle)
    }

    @Test
    fun testToggleFavouriteAddsFavouriteIfConversationIsCurrentlyNotFavourite() {
        every { conversationLocalDataSourceMock.addConversationToFavourites(1) } just runs

        val successfulToggle = repository.toggleFavourite(1, false)

        verify { conversationLocalDataSourceMock.addConversationToFavourites(1) }
        assertTrue(successfulToggle)
    }

    @Test
    fun testToggleFavouriteFailsWhenAddConversationToFavouritesThrowsException() {
        every { conversationLocalDataSourceMock.addConversationToFavourites(1)
        } throws IllegalStateException("Max favourites reached")

        val result = repository.toggleFavourite(1, isFavourite = false)

        assertFalse(result)
    }


    /****/

    @Test
    fun testGetConversationTitleReturnsCorrespondingDataSourceValue() {
        every { conversationLocalDataSourceMock.getConversationTitleFromId(1) } returns "Test"

        val conversationTitle = repository.getConversationTitle(1)

        assertEquals("Test", conversationTitle)
    }


    /****/

    @Test
    fun testScheduleOldConversationsDeletionDelegatesToWorker() {
        every { conversationWorkSchedulerMock.scheduleDeletion() } just runs

        repository.scheduleOldConversationsDeletion()

        verify { conversationWorkSchedulerMock.scheduleDeletion() }
    }
}