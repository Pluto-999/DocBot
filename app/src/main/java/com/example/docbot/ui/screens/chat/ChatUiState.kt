package com.example.docbot.ui.screens.chat

import androidx.compose.material3.SnackbarHostState
import com.example.docbot.data.models.MessageType

data class ChatUiState(
    val title: String = "",
    val openUpdateConversationTitleDialog: Boolean = false,
    val openDocumentPickerSheet: Boolean = false,
    val messages: List<MessageState> = listOf(),
    val currentUserMessage: String = "",
    val currentResponseMessage: String = "",
    val documentNames: List<String> = listOf(),
//    val documentProcessing: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)

data class MessageState(
    val contents: String = "",
    val messageType: MessageType = MessageType.UNKNOWN
)