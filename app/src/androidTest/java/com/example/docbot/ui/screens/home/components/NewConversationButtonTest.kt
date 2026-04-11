package com.example.docbot.ui.screens.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NewConversationButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setNewConversationButton(createConversation: () -> Unit = {}) {
        composeTestRule.setContent {
            NewConversationButton(createConversation = createConversation)
        }
    }


    /****/

    @Test
    fun testNewConversationButtonContentsAreDisplayed() {
        setNewConversationButton()

        composeTestRule.onNodeWithText("New Conversation").assertExists()
        composeTestRule.onNodeWithContentDescription("Create Conversation").assertExists()
    }

    @Test
    fun testClickingButtonTriggersCreateConversationCallback() {
        var clicked = false
        setNewConversationButton(createConversation = { clicked = true })

        composeTestRule.onNodeWithText("New Conversation").performClick()

        assertTrue(clicked)
    }
}