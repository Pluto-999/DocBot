package com.example.docbot.ui.screens.chat

import android.net.Uri
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.models.ProcessingStatus
import com.example.docbot.ui.screens.FakeConversationRepository
import com.example.docbot.ui.screens.FakeDocumentRepository
import com.example.docbot.ui.screens.FakeMessageRepository
import com.example.docbot.ui.screens.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    private lateinit var fakeConversationRepository: FakeConversationRepository
    private lateinit var fakeMessageRepository: FakeMessageRepository
    private lateinit var fakeDocumentRepository: FakeDocumentRepository

    private lateinit var viewModel: ChatViewModel
    private val conversationId: Long = 1

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        fakeConversationRepository = FakeConversationRepository()
        fakeMessageRepository = FakeMessageRepository()
        fakeDocumentRepository = FakeDocumentRepository()

        val conversationId = fakeConversationRepository.createConversation()

        viewModel = ChatViewModel(
            fakeConversationRepository,
            fakeMessageRepository,
            fakeDocumentRepository,
            conversationId,
            mainDispatcherRule.testDispatcher
        )
    }


    /****/

    @Test
    fun testGetConversationTitleWhenNullOnInit() {
        fakeConversationRepository.returnNullTitle = true

        viewModel = ChatViewModel(
            fakeConversationRepository,
            fakeMessageRepository,
            fakeDocumentRepository,
            conversationId,
            mainDispatcherRule.testDispatcher
        )

        assertEquals("Untitled", viewModel.uiState.value.title)
    }

    @Test
    fun testGetMessagesOnInit() = runTest {
        fakeMessageRepository.createPrompt(conversationId, "prompt 1")
        fakeMessageRepository.saveResponse(conversationId, "response 1")

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.messages.size)

        assertEquals("prompt 1", viewModel.uiState.value.messages[0].contents)
        assertEquals(MessageType.PROMPT, viewModel.uiState.value.messages[0].messageType)

        assertEquals("response 1", viewModel.uiState.value.messages[1].contents)
        assertEquals(MessageType.RESPONSE, viewModel.uiState.value.messages[1].messageType)
    }

    @Test
    fun testGetDocumentNamesOnInit() = runTest {
        fakeDocumentRepository.addDocumentTitles("Test 1")

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.documentNames.size)
        assertEquals("Test 1", viewModel.uiState.value.documentNames[0])

        fakeDocumentRepository.addDocumentTitles("Test 2")

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.documentNames.size)
        assertEquals("Test 2", viewModel.uiState.value.documentNames[1])
    }

    @Test
    fun testGetDocumentProcessingStateIsTrueWithSingleDocumentOnInit() = runTest {
        fakeDocumentRepository.updateProcessingStatus(1, ProcessingStatus.PROCESSING)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.documentProcessing)
    }

    @Test
    fun testGetDocumentProcessingStateIsTrueWithMultipleDocumentsOnInit() = runTest {
        fakeDocumentRepository.updateProcessingStatus(1, ProcessingStatus.PROCESSING)
        fakeDocumentRepository.updateProcessingStatus(2, ProcessingStatus.QUEUED)
        fakeDocumentRepository.updateProcessingStatus(3, ProcessingStatus.COMPLETED)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.documentProcessing)
    }

    @Test
    fun testGetDocumentProcessingStateIsFalseOnInit() = runTest {
        fakeDocumentRepository.updateProcessingStatus(1, ProcessingStatus.FAILED)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.documentProcessing)
    }


    /****/

    @Test
    fun testToggleUpdateConversationTitleDialog() {
        assertFalse(viewModel.uiState.value.openUpdateConversationTitleDialog)

        viewModel.toggleUpdateConversationTitleDialog(true)

        assertTrue(viewModel.uiState.value.openUpdateConversationTitleDialog)
    }


    /****/

    @Test
    fun testUpdateConversationTitle() {
        viewModel.updateConversationTitle("Test")

        assertEquals("Test", viewModel.uiState.value.title)
    }


    /****/

    @Test
    fun testSendMessageClearsCurrentMessageAndSetsModelProcessingAsTrue() = runTest {
        assertEquals(0, viewModel.uiState.value.messages.size)

        viewModel.updateCurrentMessage("Test")
        assertEquals("Test", viewModel.uiState.value.currentUserMessage)

        viewModel.sendMessage()

        assertEquals("", viewModel.uiState.value.currentUserMessage)
        assertTrue(viewModel.uiState.value.modelProcessing)
    }


    @Test
    fun testSendMessageGeneratesResponseAndSavesOnCompletion() = runTest {
        viewModel.updateCurrentMessage("Test")
        viewModel.sendMessage()

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.messages.size)

        assertEquals("Test", viewModel.uiState.value.messages[0].contents)
        assertEquals(MessageType.PROMPT, viewModel.uiState.value.messages[0].messageType)

        assertEquals("Fake response", viewModel.uiState.value.messages[1].contents)
        assertEquals(MessageType.RESPONSE, viewModel.uiState.value.messages[1].messageType)
    }

    @Test
    fun testSendMessageClearsCurrentResponseMessageAndSetsModelProcessingAsFalseOnCompletion() = runTest {
        viewModel.sendMessage()

        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.currentUserMessage)
        assertFalse(viewModel.uiState.value.modelProcessing)
    }


    /****/

    @Test
    fun testToggleDocumentPickerDialog() {
        viewModel.toggleDocumentPickerDialog(true)
        assertTrue(viewModel.uiState.value.openDocumentPickerSheet)
        viewModel.toggleDocumentPickerDialog(false)
        assertFalse(viewModel.uiState.value.openDocumentPickerSheet)
    }


    /****/

    @Test
    fun testSuccessfulProcessDocumentDoesNotSetErrorMessage() = runTest {
        viewModel.processDocument(mockk<Uri>())

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.openDocumentPickerSheet)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun testUnsuccessfulProcessDocumentSetsErrorMessage() = runTest {
        fakeDocumentRepository.successfulProcess = false

        viewModel.processDocument(mockk<Uri>())

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.openDocumentPickerSheet)
        assertEquals(
            "Something went wrong. Please ensure you have selected no more than 5 documents, and try again.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun testProcessDocumentWithNullUriDoesNothing() {
        viewModel.processDocument(null)

        assertFalse(viewModel.uiState.value.openDocumentPickerSheet)
    }


    /****/

    @Test
    fun testDisplayBackMessageAndClearErrorMessage() {
        assertNull(viewModel.uiState.value.errorMessage)

        viewModel.displayBackMessage()

        assertEquals(
            "Please wait until the model has finished generating its response before navigating back.",
            viewModel.uiState.value.errorMessage
        )

        viewModel.clearErrorMessage()

        assertNull(viewModel.uiState.value.errorMessage)
    }
}