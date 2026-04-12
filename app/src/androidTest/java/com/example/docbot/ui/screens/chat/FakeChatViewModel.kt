package com.example.docbot.ui.screens.chat

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeChatViewModel: ChatViewModel {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            title = "test conversation title",
            messages = listOf(
                MessageState(contents = "message 1"),
                MessageState(contents = "message 2")
            )
        )
    )

    override val uiState: StateFlow<ChatUiState> = _uiState

    fun setUiState(newState: ChatUiState) {
        _uiState.value = newState
    }

    var updateTitleOpen = false
    override fun toggleUpdateConversationTitleDialog(isOpen: Boolean) {
        updateTitleOpen = isOpen
    }

    var updatedTitle = ""
    override fun updateConversationTitle(newTitle: String) {
        updatedTitle = newTitle
    }

    var messageSent = false
    override fun sendMessage() {
        messageSent = true
    }

    var currentMessage = ""
    override fun updateCurrentMessage(newMessage: String) {
        currentMessage = newMessage
    }

    var documentPickerOpen = false
    override fun toggleDocumentPickerDialog(isOpen: Boolean) {
        documentPickerOpen = isOpen
    }

    override fun processDocument(uri: Uri?) {

    }

    override fun getDocumentNames() {

    }

    var displayBackMessage = false
    override fun displayBackMessage() {
        displayBackMessage = true
    }

    var errorCleared = false
    override fun clearErrorMessage() {
        errorCleared = true
    }

}