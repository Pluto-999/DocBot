package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MessageTextBoxTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setMessageTextBox(
        value: String = "",
        onValueChange: (String) -> Unit = {},
        createMessage: () -> Unit = {},
        enabled: Boolean = true
    ) {
        composeTestRule.setContent {
            MessageTextBox(value, onValueChange, createMessage, enabled)
        }
    }


    /****/

    @Test
    fun testMessageTextBoxContentsAreDisplayed() {
        setMessageTextBox(value = "message")

        composeTestRule.onNodeWithText("message").assertExists()
        composeTestRule.onNodeWithText("Enter message ...").assertDoesNotExist()
    }

    @Test
    fun testPlaceholderDisplayedWhenValueIsEmpty() {
        setMessageTextBox()

        composeTestRule.onNodeWithText("Enter message ...").assertExists()
    }

    @Test
    fun testMessageTextBoxIsDisabledWhenEnabledIsFalse() {
        setMessageTextBox(enabled = false)

        composeTestRule.onNodeWithText("Enter message ...").assertIsNotEnabled()
    }

    @Test
    fun testOnValueChangeTriggersCallback() {
        var inputValue = ""
        setMessageTextBox(onValueChange = { inputValue = it })

        composeTestRule.onNodeWithText("Enter message ...").performTextInput("test")

        assertEquals("test", inputValue)
    }

    @Test
    fun testCreateMessageCallbackIsTriggeredOnImeActionDone() {
        var triggered = false
        setMessageTextBox(createMessage = { triggered = true })

        composeTestRule.onNodeWithText("Enter message ...").performImeAction()

        assertTrue(triggered)
    }
}