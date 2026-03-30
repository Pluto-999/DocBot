package com.example.docbot.ui.screens.home

import com.example.docbot.ui.screens.FakeConversationRepository
import com.example.docbot.ui.screens.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class HomeViewModelTest {

    private lateinit var fakeRepository: FakeConversationRepository
    private lateinit var viewModel: HomeViewModel

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        fakeRepository = FakeConversationRepository()
        viewModel = HomeViewModel(fakeRepository)
    }


    /****/

    @Test
    fun testGetConversationsOnInit() = runTest {
        fakeRepository.createConversation()
        fakeRepository.createConversation()

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.conversations.size)
    }


    /****/

    @Test
    fun testOldConversationIsMarkedWithDeleteSoon() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now().minusDays(9))
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertTrue(conversation.deleteSoon)
    }

    @Test
    fun testNotOldConversationIsNotMarkedWithDeleteSoon() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now().minusDays(5))
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertFalse(conversation.deleteSoon)
    }

    /****/

    @Test
    fun testFormatDateAsJustNow() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now())
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertEquals("Just now", conversation.date)
    }

    @Test
    fun testFormatDateAsMinutesAgo() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now().minusMinutes(10))
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertEquals("10 minutes ago", conversation.date)
    }

    @Test
    fun testFormatDateAsHoursAgo() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now().minusHours(10))
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertEquals("10 hours ago", conversation.date)
    }

    @Test
    fun testFormatDateAsOneDayAgo() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now().minusDays(1))
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertEquals("1 day ago", conversation.date)
    }

    @Test
    fun testFormatDateAsDaysAgo() = runTest {
        fakeRepository.createConversationWithLatestMessage(LocalDateTime.now().minusDays(5))
        advanceUntilIdle()

        val conversation = viewModel.uiState.value.conversations.first()
        assertEquals("5 days ago", conversation.date)
    }


    /****/

    @Test
    fun testCreateConversation() = runTest {
        assertEquals(0, viewModel.uiState.value.conversations.size)

        viewModel.createConversation()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.conversations.size)

        viewModel.createConversation()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.conversations.size)
    }


    /****/

    @Test
    fun testDeleteConversation() = runTest {
        val conversationOneId = fakeRepository.createConversation()
        val conversationTwoId = fakeRepository.createConversation()

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.conversations.size)

        viewModel.deleteConversation(conversationOneId)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.conversations.size)

        viewModel.deleteConversation(conversationTwoId)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.conversations.size)
    }


    /****/

    @Test
    fun testToggleFavouriteWithSuccessfulToggle() {
        val conversationId = fakeRepository.createConversation()

        viewModel.toggleFavourite(conversationId, true)

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testToggleFavouriteUpdatesErrorMessageWithUnsuccessfulToggle() {
        val conversationId = fakeRepository.createConversation()

        fakeRepository.favouriteToggleSuccess = false

        viewModel.toggleFavourite(conversationId, true)

        assertEquals(
            viewModel.uiState.value.errorMessage,
            "You can only have up to 10 favourite conversations"
        )

        viewModel.clearErrorMessage()

        assertNull(viewModel.uiState.value.errorMessage)
    }



    /****/

    @Test
    fun testToggleFilterMenu() {
        viewModel.toggleFilterMenu(true)
        assertTrue(viewModel.uiState.value.filterMenuExpanded)
        viewModel.toggleFilterMenu(false)
        assertFalse(viewModel.uiState.value.filterMenuExpanded)
    }


    /****/

    @Test
    fun testToggleSortMenuOpens() {
        viewModel.toggleSortMenu(true)
        assertTrue(viewModel.uiState.value.sortMenuExpanded)
        viewModel.toggleSortMenu(false)
        assertFalse(viewModel.uiState.value.sortMenuExpanded)
    }


    /****/

    @Test
    fun testUpdateConversationOrder() {
        viewModel.updateConversationOrder(ConversationOrder.TITLE_ASC)
        assertEquals(
            ConversationOrder.TITLE_ASC,
            viewModel.uiState.value.conversationOrder
        )
    }


    /****/

    @Test
    fun testUpdateConversationFilter() {
        viewModel.updateConversationFilter(ConversationFilter.FAVOURITES)
        assertEquals(
            ConversationFilter.FAVOURITES,
            viewModel.uiState.value.conversationFilter
        )
    }


    /****/

    @Test
    fun testUpdateSearchQuery() {
        viewModel.updateSearchQuery("test")
        assertEquals("test", viewModel.uiState.value.searchQuery)
    }
}