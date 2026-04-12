package com.example.docbot.ui.screens.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeViewModel: FakeHomeViewModel = FakeHomeViewModel()

    private fun setHomeScreen(
        fakeHomeViewModel: FakeHomeViewModel = fakeViewModel,
        onConversationNavigate: (id: Long) -> Unit = {}
    ) {
        composeTestRule.setContent {
            HomeScreen(fakeHomeViewModel, onConversationNavigate)
        }
    }


    /****/

    @Test
    fun testNewConversationButtonIsDisplayed() {
        setHomeScreen()
        composeTestRule.onNodeWithText("New Conversation").assertExists()
    }

    @Test
    fun testSearchBarIsDisplayed() {
        setHomeScreen()
        composeTestRule.onNodeWithText("Search").assertExists()
    }

    @Test
    fun testConversationCardsAreDisplayedFromUiState() {
        setHomeScreen()
        composeTestRule.onNodeWithText("title 1").assertExists()
        composeTestRule.onNodeWithText("title 2").assertExists()
        composeTestRule.onNodeWithText("title 3").assertExists()
    }


    /****/

    @Test
    fun testSnackbarWithErrorMessageDisplayedWhenUiStateHasErrorMessage() {
        fakeViewModel.setUiState(HomeUiState(errorMessage = "error"))
        setHomeScreen()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("error").assertExists()
    }

    @Test
    fun testClearErrorMessageTriggeredAfterErrorSnackbarHasBeenDisplayed() {
        fakeViewModel.setUiState(HomeUiState(errorMessage = "error"))
        setHomeScreen()
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            fakeViewModel.errorCleared
        }
        assertTrue(fakeViewModel.errorCleared)
    }

    /****/

    @Test
    fun testClickingSortMenuIconTogglesSortMenu() {
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        assertTrue(fakeViewModel.sortMenuToggled)
    }

    @Test
    fun testCollapseSortMenuTriggeredWhenDismissed() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true))
        setHomeScreen()
        Espresso.pressBack()
        assertTrue(fakeViewModel.sortMenuCollapsed)
    }

    @Test
    fun testClickingFilterMenuIconTogglesFilterMenu() {
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Filter").performClick()
        assertTrue(fakeViewModel.filterMenuToggled)
    }

    @Test
    fun testCollapseFilterMenuTriggeredWhenDismissed() {
        fakeViewModel.setUiState(HomeUiState(filterMenuExpanded = true))
        setHomeScreen()
        Espresso.pressBack()
        assertTrue(fakeViewModel.filterMenuCollapsed)
    }


    /****/

    @Test
    fun testTitleIsDescendingAsDefault() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Title Descending").assertExists()
    }

    @Test
    fun testDateIsDescendingAsDefault() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Date Descending").assertExists()

    }

    @Test
    fun testTitleIsAscendingWhenSpecifiedByUiState() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true, conversationOrder = ConversationOrder.TITLE_ASC))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Title Ascending").assertExists()
    }

    @Test
    fun testDateIsAscendingWhenSpecifiedByUiState() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true, conversationOrder = ConversationOrder.DATE_ASC))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Date Ascending").assertExists()
    }


    /****/

    @Test
    fun testClickingTitleTriggersUpdateTitleOrder() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Title Descending").performClick()
        assertTrue(fakeViewModel.titleOrderUpdated)
    }

    @Test
    fun testClickingDateTriggersUpdateDateOrder() {
        fakeViewModel.setUiState(HomeUiState(sortMenuExpanded = true))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Date Descending").performClick()
        assertTrue(fakeViewModel.dateOrderUpdated)
    }


    /****/

    @Test
    fun testClickingFavouriteInDropdownTriggersFilteringOfFavouriteConversations() {
        fakeViewModel.setUiState(HomeUiState(filterMenuExpanded = true))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Favourites").performClick()
        assertEquals(ConversationFilter.FAVOURITES, fakeViewModel.filterSelected)
    }

    @Test
    fun testClickingDeleteInDropdownTriggersFilteringOfSoonToDeleteConversations() {
        fakeViewModel.setUiState(HomeUiState(filterMenuExpanded = true))
        setHomeScreen()
        composeTestRule.onNodeWithContentDescription("Soon to be deleted").performClick()
        assertEquals(ConversationFilter.DELETE_SOON, fakeViewModel.filterSelected)
    }

    /****/

    @Test
    fun testSearchQueryInputTriggersUpdateSearchQuery() {
        setHomeScreen()
        composeTestRule.onNodeWithText("Search").performTextInput("test")
        assertEquals("test", fakeViewModel.searchQuery)
    }


    /****/

    @Test
    fun testFavouriteConversationTriggersToggleFavourite() {
        fakeViewModel.setUiState(HomeUiState(conversations = listOf(
            ConversationState(id = 1L, title = "Chat 1", date = "1 Jan", isFavourite = false)
        )))
        setHomeScreen()

        composeTestRule.onNodeWithContentDescription("Not Favourite").performClick()

        assertEquals(1L, fakeViewModel.toggledId)
    }

    @Test
    fun testDeleteConversationTriggersDeleteConversation() {
        fakeViewModel.setUiState(HomeUiState(conversations = listOf(
            ConversationState(id = 1L, title = "Chat 1", date = "1 Jan", isFavourite = false)
        )))
        setHomeScreen()

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        assertEquals(1L, fakeViewModel.deletedId)
    }


    /****/

    @Test
    fun testClickingConversationCardTriggersOnConversationNavigateCallback() {
        var id = -1L
        setHomeScreen(onConversationNavigate = { id = it })

        composeTestRule.onNodeWithText("title 1").performClick()

        assertEquals(1, id)
    }

    @Test
    fun testClickingNewConversationButtonTriggersOnConversationNavigateCallback() {
        var id = -1L
        setHomeScreen(onConversationNavigate = { id = it })

        composeTestRule.onNodeWithText("New Conversation").performClick()

        assertEquals(100, id)
    }
}