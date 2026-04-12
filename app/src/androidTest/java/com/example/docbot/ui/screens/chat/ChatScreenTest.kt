package com.example.docbot.ui.screens.chat

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeViewModel: FakeChatViewModel = FakeChatViewModel()

    private fun setChatScreen(
        conversationId: Long = 1,
        fakeChatViewModel: FakeChatViewModel = fakeViewModel
    ) {
        composeTestRule.setContent {
            ChatScreen(conversationId, fakeChatViewModel)
        }
    }


    /****/

    @Test
    fun testConversationTitleIsDisplayed() {
        setChatScreen()
        composeTestRule.onNodeWithText("test conversation title").assertExists()
    }

    @Test
    fun testIconsInAppBarAreDisplayed() {
        setChatScreen()
        composeTestRule.onNodeWithContentDescription("Edit Title").assertExists()
        composeTestRule.onNodeWithContentDescription("Add PDFs").assertExists()
    }

    @Test
    fun testMessagesAreDisplayed() {
        setChatScreen()
        composeTestRule.onNodeWithText("message 1").assertExists()
        composeTestRule.onNodeWithText("message 2").assertExists()
    }

    @Test
    fun testCurrentResponseMessageIsDisplayed() {
        fakeViewModel.setUiState(ChatUiState(currentResponseMessage = "current message"))
        setChatScreen()
        composeTestRule.onNodeWithText("current message").assertExists()
    }

    @Test
    fun testCurrentUserMessageIsDisplayed() {
        fakeViewModel.setUiState(ChatUiState(currentUserMessage = "current message"))
        setChatScreen()
        composeTestRule.onNodeWithText("current message").assertExists()
    }


    /****/

    @Test
    fun testErrorMessageIsDisplayed() {
        fakeViewModel.setUiState(ChatUiState(errorMessage = "error"))
        setChatScreen()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("error").assertExists()
    }

    @Test
    fun testClearErrorMessageTriggeredAfterErrorSnackbarHasBeenDisplayed() {
        fakeViewModel.setUiState(ChatUiState(errorMessage = "error"))
        setChatScreen()
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            fakeViewModel.errorCleared
        }
        assertTrue(fakeViewModel.errorCleared)
    }


    /****/

    @Test
    fun testBackHandlerTriggersDisplayBackMessageWhenModelIsInferring() {
        fakeViewModel.setUiState(ChatUiState(modelInference = true))
        setChatScreen()
        Espresso.pressBack()
        assertTrue(fakeViewModel.displayBackMessage)
    }

    @Test
    fun testBackHandlerTriggersDisplayBackMessageWhenModelIsResponding() {
        fakeViewModel.setUiState(ChatUiState(modelResponding = true))
        setChatScreen()
        Espresso.pressBack()
        assertTrue(fakeViewModel.displayBackMessage)
    }


    /****/

    @Test
    fun testClickingEditTitleButtonTriggersAssociatedViewModelFunction() {
        setChatScreen()
        composeTestRule.onNodeWithContentDescription("Edit Title").performClick()
        assertTrue(fakeViewModel.updateTitleOpen)
    }

    @Test
    fun testClickingPdfButtonWhenNotDisabledTriggersAssociatedViewModelFunction() {
        setChatScreen()
        composeTestRule.onNodeWithContentDescription("Add PDFs").performClick()
        assertTrue(fakeViewModel.documentPickerOpen)
    }

    @Test
    fun testClickingPdfButtonWhenDisabledDoesNotTriggerAssociatedViewModelFunction() {
        fakeViewModel.setUiState(ChatUiState(documentProcessing = true))
        setChatScreen()
        composeTestRule.onNodeWithContentDescription("Add PDFs").performClick()
        assertFalse(fakeViewModel.documentPickerOpen)
    }


    /****/

    @Test
    fun testMessageTextBoxIsEnabledByDefault() {
        setChatScreen()
        composeTestRule.onNodeWithText("Enter message ...").assertIsEnabled()
    }

    @Test
    fun testMessageTextBoxIsDisabledAccordingToUiState() {
        fakeViewModel.setUiState(ChatUiState(documentProcessing = true))
        setChatScreen()
        composeTestRule.onNodeWithText("Enter message ...").assertIsNotEnabled()
    }

    @Test
    fun testCreatingMessageTriggersSendMessageFunction() {
        setChatScreen()
        composeTestRule.onNodeWithText("Enter message ...").performImeAction()
        assertTrue(fakeViewModel.messageSent)
    }

    @Test
    fun testInputtingMessageTriggersUpdateCurrentMessage() {
        setChatScreen()
        composeTestRule.onNodeWithText("Enter message ...").performTextInput("test")
        assertEquals("test", fakeViewModel.currentMessage)
    }


    /****/

    @Test
    fun testConversationTitleDialogIsDisplayedWhenTrueInUiState() {
        fakeViewModel.setUiState(ChatUiState(
            openUpdateConversationTitleDialog = true,
            title = "current title"
        ))
        setChatScreen()
        composeTestRule.onNodeWithText("Change conversation name").assertExists()
        composeTestRule.onAllNodesWithText("current title").assertCountEquals(2)
    }

    @Test
    fun testDismissingConversationTitleDialogTriggersAssociatedViewModelFunction() {
        fakeViewModel.setUiState(ChatUiState(openUpdateConversationTitleDialog = true))
        setChatScreen()
        composeTestRule.onNodeWithText("Change conversation name").performImeAction()
        assertFalse(fakeViewModel.updateTitleOpen)
    }

    @Test
    fun testUpdatingConversationTitleInDialogTriggersAssociatedViewModelFunction() {
        fakeViewModel.setUiState(ChatUiState(openUpdateConversationTitleDialog = true))
        setChatScreen()
        composeTestRule.onNodeWithText("Change conversation name").performTextInput("new title")
        assertEquals("new title", fakeViewModel.updatedTitle)
    }


    /****/

    @Test
    fun testDocumentPickerSheetIsDisplayedWhenTrueInUiState() {
        fakeViewModel.setUiState(ChatUiState(
            openDocumentPickerSheet = true,
            documentNames = listOf("doc 1", "doc 2")
        ))
        setChatScreen()
        composeTestRule.onNodeWithText("Choose Documents").assertExists()
        composeTestRule.onNodeWithText("doc 1").assertExists()
        composeTestRule.onNodeWithText("doc 2").assertExists()
    }

    @Test
    fun testDismissingDocumentPickerSheetTriggersAssociatedViewModelFunction() {
        fakeViewModel.setUiState(ChatUiState(openDocumentPickerSheet = true))
        setChatScreen()
        Espresso.pressBack()
        assertFalse(fakeViewModel.documentPickerOpen)
    }


    /****/

    @Test
    fun testDocumentProcessingIndicatorIsDisplayedWhenTrueInUiState() {
        fakeViewModel.setUiState(ChatUiState(documentProcessing = true))
        setChatScreen()
        composeTestRule.onNodeWithText("Processing document").assertExists()
    }

    @Test
    fun testModelInferenceIndicatorIsDisplayedWhenTrueInUiState() {
        fakeViewModel.setUiState(ChatUiState(modelInference = true))
        setChatScreen()
        composeTestRule.onNodeWithText("Model generating response").assertExists()
    }
}