package com.example.docbot.ui.screens.chat.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.docbot.data.models.MessageType
import com.example.docbot.ui.screens.chat.MessageState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MessageListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val messagesList = listOf(
        MessageState(contents = "message 1", messageType = MessageType.PROMPT),
        MessageState(contents = "message 2", messageType = MessageType.RESPONSE),
        MessageState(contents = "message 3", messageType = MessageType.PROMPT),
        MessageState(contents = "message 4", messageType = MessageType.RESPONSE),
    )

    private fun setMessageList(
        messages: List<MessageState> = messagesList,
        currentResponse: String = "",
        listState: LazyListState = LazyListState(),
    ) {
        composeTestRule.setContent {
            MessageList(messages, currentResponse, listState)
        }
    }


    /****/

    @Test
    fun testListOfMessagesDisplayed() {
        setMessageList()
        for (message in messagesList) {
            composeTestRule.onNodeWithText(message.contents).assertExists()
        }
    }

    @Test
    fun testEmptyMessagesListDisplaysNoMessages() {
        setMessageList(messages = emptyList())
        for (message in messagesList) {
            composeTestRule.onNodeWithText(message.contents).assertDoesNotExist()
        }
    }

    @Test
    fun testCurrentResponseNotDisplayedWhenEmpty() {
        setMessageList()
        composeTestRule.onNodeWithText("").assertDoesNotExist()
    }

    @Test
    fun testCurrentResponseDisplayedWhenNotEmpty() {
        setMessageList(currentResponse = "test")
        composeTestRule.onNodeWithText("test").assertExists()
    }
}