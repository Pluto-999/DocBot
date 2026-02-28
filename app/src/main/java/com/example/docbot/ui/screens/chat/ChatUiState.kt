package com.example.docbot.ui.screens.chat

data class ChatUiState(
    val title: String = "",
    val openUpdateConversationTitleDialog: Boolean = false,
    val openDocumentPickerDialog: Boolean = false,
    val messages: List<MessageState> = listOf(),
    val currentMessage: String = "",
    val documentNames: List<String> = listOf()
)

data class MessageState(
    val contents: String = "",
    val messageType: String = ""
)