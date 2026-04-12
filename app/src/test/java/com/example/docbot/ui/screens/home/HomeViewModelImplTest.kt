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

class HomeViewModelImplTest {

    private lateinit var fakeRepository: FakeConversationRepository
    private lateinit var viewModel: HomeViewModelImpl

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        fakeRepository = FakeConversationRepository()
        viewModel = HomeViewModelImpl(fakeRepository)
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
        assertFalse(viewModel.uiState.value.filterMenuExpanded)
        viewModel.toggleFilterMenu()
        assertTrue(viewModel.uiState.value.filterMenuExpanded)
        viewModel.toggleFilterMenu()
        assertFalse(viewModel.uiState.value.filterMenuExpanded)
    }


    /****/

    @Test
    fun testCollapseFilterMenu() {
        viewModel.toggleFilterMenu()
        assertTrue(viewModel.uiState.value.filterMenuExpanded)
        viewModel.collapseFilterMenu()
        assertFalse(viewModel.uiState.value.filterMenuExpanded)
    }


    /****/

    @Test
    fun testToggleSortMenu() {
        assertFalse(viewModel.uiState.value.sortMenuExpanded)
        viewModel.toggleSortMenu()
        assertTrue(viewModel.uiState.value.sortMenuExpanded)
        viewModel.toggleSortMenu()
        assertFalse(viewModel.uiState.value.sortMenuExpanded)
    }


    /****/

    @Test
    fun testCollapseSortMenu() {
        viewModel.toggleSortMenu()
        assertTrue(viewModel.uiState.value.sortMenuExpanded)
        viewModel.collapseSortMenu()
        assertFalse(viewModel.uiState.value.sortMenuExpanded)
    }


    /****/

    @Test
    fun testUpdateTitleOrder() {
        assertNotEquals(ConversationOrder.TITLE_ASC, viewModel.uiState.value.conversationOrder)
        viewModel.updateTitleOrder()
        assertEquals(ConversationOrder.TITLE_ASC, viewModel.uiState.value.conversationOrder)
        viewModel.updateTitleOrder()
        assertEquals(ConversationOrder.TITLE_DESC, viewModel.uiState.value.conversationOrder)
    }


    /****/

    @Test
    fun testUpdateDateOrder() {
        assertEquals(ConversationOrder.DATE_DESC, viewModel.uiState.value.conversationOrder)
        viewModel.updateDateOrder()
        assertEquals(ConversationOrder.DATE_ASC, viewModel.uiState.value.conversationOrder)
        viewModel.updateDateOrder()
        assertEquals(ConversationOrder.DATE_DESC, viewModel.uiState.value.conversationOrder)
    }


    /****/

    @Test
    fun testFilterConversationsOnFavourite() {
        assertEquals(ConversationFilter.NONE, viewModel.uiState.value.conversationFilter)
        viewModel.filterConversations(ConversationFilter.FAVOURITES)
        assertEquals(ConversationFilter.FAVOURITES, viewModel.uiState.value.conversationFilter)
        viewModel.filterConversations(ConversationFilter.FAVOURITES)
        assertEquals(ConversationFilter.NONE, viewModel.uiState.value.conversationFilter)
    }

    @Test
    fun testFilterConversationsOnDeleteSoon() {
        assertEquals(ConversationFilter.NONE, viewModel.uiState.value.conversationFilter)
        viewModel.filterConversations(ConversationFilter.DELETE_SOON)
        assertEquals(ConversationFilter.DELETE_SOON, viewModel.uiState.value.conversationFilter)
        viewModel.filterConversations(ConversationFilter.DELETE_SOON)
        assertEquals(ConversationFilter.NONE, viewModel.uiState.value.conversationFilter)
    }

    @Test
    fun testFilterConversationsInteractions() {
        assertEquals(ConversationFilter.NONE, viewModel.uiState.value.conversationFilter)
        viewModel.filterConversations(ConversationFilter.DELETE_SOON)
        assertEquals(ConversationFilter.DELETE_SOON, viewModel.uiState.value.conversationFilter)
        viewModel.filterConversations(ConversationFilter.FAVOURITES)
        assertEquals(ConversationFilter.FAVOURITES, viewModel.uiState.value.conversationFilter)
    }


    @Test
    fun testUpdateSearchQuery() {
        viewModel.updateSearchQuery("test")
        assertEquals("test", viewModel.uiState.value.searchQuery)
    }
}