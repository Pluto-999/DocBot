package com.example.docbot.ui.screens.chat

data class ChatUiState(
    val title: String = "",
    val openUpdateConversationTitleDialog: Boolean = false,
    val messages: List<MessageState> = listOf(),
    val currentMessage: String = ""
)

data class MessageState(
    val contents: String = "",
    val messageType: String = ""
)