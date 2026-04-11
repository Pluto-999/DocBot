package com.example.docbot.ui.screens.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ConversationCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setConversationCard(
        title: String = "title",
        date: String = "date",
        isFavourite: Boolean = false,
        openConversation: () -> Unit = {},
        favouriteClick: () -> Unit = {},
        deleteClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            ConversationCard(title, date, isFavourite, openConversation, favouriteClick, deleteClick)
        }
    }


    /****/

    @Test
    fun testConversationCardContentsAreDisplayed() {
        setConversationCard()
        composeTestRule.onNodeWithText("title").assertExists()
        composeTestRule.onNodeWithText("date").assertExists()
        composeTestRule.onNodeWithContentDescription("Not Favourite").assertExists()
        composeTestRule.onNodeWithContentDescription("Delete").assertExists()
    }

    @Test
    fun testFavouriteIconChangesWhenConversationIsFavourite() {
        setConversationCard(isFavourite = true)
        composeTestRule.onNodeWithContentDescription("Favourite").assertExists()
    }

    @Test
    fun testClickingFavouriteTriggersCallback() {
        var clicked = false
        setConversationCard(favouriteClick = { clicked = true })

        composeTestRule.onNodeWithContentDescription("Not Favourite").performClick()

        assert(clicked)
    }

    @Test
    fun testClickingDeleteTriggersCallback() {
        var clicked = false
        setConversationCard(deleteClick = { clicked = true })

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        assert(clicked)
    }

    @Test
    fun testClickingOnTheCardTriggersOpenConversationCallback() {
        var clicked = false
        setConversationCard(openConversation = { clicked = true })

        composeTestRule.onNodeWithText("title").performClick()

        assert(clicked)
    }

    @Test
    fun testOpenConversationIsNotCalledWithoutClick() {
        var clicked = false
        setConversationCard(openConversation = { clicked = true })
        assert(!clicked)
    }

    @Test
    fun testFavouriteNotCalledWithoutClick() {
        var clicked = false
        setConversationCard(favouriteClick = { clicked = true })
        assert(!clicked)
    }

    @Test
    fun testDeleteNotCalledWithoutClick() {
        var clicked = false
        setConversationCard(deleteClick = { clicked = true })
        assert(!clicked)
    }
}