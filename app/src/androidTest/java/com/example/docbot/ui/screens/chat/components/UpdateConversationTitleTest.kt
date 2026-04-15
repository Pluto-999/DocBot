package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class UpdateConversationTitleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setUpdateConversationTitle(
        value: String = "",
        onValueChange: (String) -> Unit = {},
        onDismissRequest: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            UpdateConversationTitle(value, onValueChange, onDismissRequest)
        }
    }


    /****/

    @Test
    fun testUpdateConversationTitleIsDisplayed() {
        setUpdateConversationTitle()

        composeTestRule.onNodeWithText("Change conversation name").assertExists()
    }

    @Test
    fun testPlaceholderIsDisplayedAndHasEmptyValueWhenClicked() {
        setUpdateConversationTitle()

        composeTestRule.onNodeWithText("Change conversation name").performClick()
        composeTestRule.onNodeWithText("New name").assertExists()
    }

    @Test
    fun testValueIsDisplayedWhenNotEmpty() {
        setUpdateConversationTitle(value = "test")

        composeTestRule.onNodeWithText("Change conversation name").performClick()
        composeTestRule.onNodeWithText("Change conversation name").assertExists()
        composeTestRule.onNodeWithText("test").assertExists()
        composeTestRule.onNodeWithText("New name").assertDoesNotExist()
    }

    @Test
    fun testOnValueChangeTriggersCallback() {
        var inputValue = ""
        setUpdateConversationTitle(onValueChange = { inputValue = it })

        composeTestRule.onNodeWithText("Change conversation name").performTextInput("test")

        assertEquals("test", inputValue)
    }

    @Test
    fun testOnDismissRequestCallbackIsTriggeredOnImeActionDone() {
        var triggered = false
        setUpdateConversationTitle(onDismissRequest = { triggered = true })

        composeTestRule.onNodeWithText("Change conversation name").performImeAction()

        assertTrue(triggered)
    }
}