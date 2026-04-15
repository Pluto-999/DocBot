package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ConversationTitleDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setConversationTitleDialog(
        isOpen: Boolean = true,
        onDismissRequest: () -> Unit = {},
        value: String = "",
        onValueChange: (title: String) -> Unit = {}
    ) {
        composeTestRule.setContent {
            ConversationTitleDialog(isOpen, onDismissRequest, value, onValueChange)
        }
    }


    /****/

    @Test
    fun testUpdateConversationTitleIsNotDisplayedWhenIsOpenIsFalse() {
        setConversationTitleDialog(isOpen = false)
        composeTestRule.onNodeWithText("Change conversation name").assertDoesNotExist()
    }

    @Test
    fun testUpdateConversationTitleIsDisplayedWhenIsOpenIsTrue() {
        setConversationTitleDialog()
        composeTestRule.onNodeWithText("Change conversation name").assertExists()
    }
}