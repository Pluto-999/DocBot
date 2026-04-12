package com.example.docbot.ui.screens.chat

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface ChatViewModel {
    val uiState: StateFlow<ChatUiState>

    fun toggleUpdateConversationTitleDialog(isOpen: Boolean)
    fun updateConversationTitle(newTitle: String)
    fun sendMessage()
    fun updateCurrentMessage(newMessage: String)
    fun toggleDocumentPickerDialog(isOpen: Boolean)
    fun processDocument(uri: Uri?)
    fun getDocumentNames()
    fun displayBackMessage()
    fun clearErrorMessage()
}
