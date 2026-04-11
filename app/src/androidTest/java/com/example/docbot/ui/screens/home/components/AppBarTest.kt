package com.example.docbot.ui.screens.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AppBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setAppBar(
        sortMenuExpanded: Boolean = false,
        filterMenuExpanded: Boolean = false,
        sortIconOnClick: () -> Unit = {},
        sortMenuDismiss: () -> Unit = {},
        filterIconOnClick: () -> Unit = {},
        filterMenuDismiss: () -> Unit = {},
        titleOnClick: () -> Unit = {},
        dateOnClick: () -> Unit = {},
        favouriteOnClick: () -> Unit = {},
        deleteSoonOnClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            AppBar(
                sortIconOnClick,
                sortMenuExpanded,
                sortMenuDismiss,
                filterIconOnClick,
                filterMenuExpanded,
                filterMenuDismiss,
                titleOnClick,
                titleIcon = { Icon(Icons.Default.Title, contentDescription = "Title") },
                dateOnClick,
                dateIcon = { Icon(Icons.Default.DateRange, contentDescription = "Date") },
                favouriteOnClick,
                deleteSoonOnClick
            )
        }
    }


    /****/

    @Test
    fun testAppBarContentsAreDisplayed() {
        setAppBar()
        composeTestRule.onNodeWithText("Conversations").assertExists()
        composeTestRule.onNodeWithContentDescription("Sort").assertExists()
        composeTestRule.onNodeWithContentDescription("Filter").assertExists()
    }


    /****/

    @Test
    fun testSortMenuIsNotVisibleWhenCollapsed() {
        setAppBar()
        composeTestRule.onNodeWithText("Title").assertDoesNotExist()
        composeTestRule.onNodeWithText("Date").assertDoesNotExist()
    }

    @Test
    fun testSortMenuIsVisibleWhenExpanded() {
        setAppBar(sortMenuExpanded = true)
        composeTestRule.onNodeWithText("Title").assertExists()
        composeTestRule.onNodeWithText("Date").assertExists()
    }

    @Test
    fun testPressingBackWhenSortMenuIsExpandedTriggersSortMenuDismiss() {
        var dismissed = false
        setAppBar(sortMenuExpanded = true, sortMenuDismiss = { dismissed = true })

        Espresso.pressBack()

        assertTrue(dismissed)
    }

    @Test
    fun testClickingSortIconInvokesSortIconOnClick() {
        var clicked = false
        setAppBar(sortIconOnClick = { clicked = true })
        composeTestRule.onNodeWithContentDescription("Sort").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testClickingTitleInvokesTitleOnClick() {
        var clicked = false
        setAppBar(sortMenuExpanded = true, titleOnClick = { clicked = true })
        composeTestRule.onNodeWithText("Title").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testClickingDateInvokesDateOnClick() {
        var clicked = false
        setAppBar(sortMenuExpanded = true, dateOnClick = { clicked = true })
        composeTestRule.onNodeWithText("Date").performClick()
        assertTrue(clicked)
    }


    /****/

    @Test
    fun testFilterMenuIsNotVisibleWhenCollapsed() {
        setAppBar()
        composeTestRule.onNodeWithText("Favourites").assertDoesNotExist()
        composeTestRule.onNodeWithText("Soon to be deleted").assertDoesNotExist()
    }

    @Test
    fun testFilterMenuIsVisibleWhenExpanded() {
        setAppBar(filterMenuExpanded = true)
        composeTestRule.onNodeWithText("Favourites").assertExists()
        composeTestRule.onNodeWithText("Soon to be deleted").assertExists()
    }

    @Test
    fun testPressingBackWhenFilterMenuIsExpandedTriggersFilterMenuDismiss() {
        var dismissed = false
        setAppBar(filterMenuExpanded = true, filterMenuDismiss = { dismissed = true })

        Espresso.pressBack()

        assertTrue(dismissed)
    }

    @Test
    fun testClickingFilterIconInvokesFilterIconOnClick() {
        var clicked = false
        setAppBar(filterIconOnClick = { clicked = true })
        composeTestRule.onNodeWithContentDescription("Filter").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testClickingFavouritesInvokesFavouriteOnClick() {
        var clicked = false
        setAppBar(filterMenuExpanded = true, favouriteOnClick = { clicked = true })
        composeTestRule.onNodeWithText("Favourites").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testClickingSoonToBeDeletedInvokesDeleteSoonOnClick() {
        var clicked = false
        setAppBar(filterMenuExpanded = true, deleteSoonOnClick = { clicked = true })
        composeTestRule.onNodeWithText("Soon to be deleted").performClick()
        assertTrue(clicked)
    }
}