package com.example.docbot.ui.screens.home

data class HomeUiState (
    val conversations: List<ConversationState> = listOf(),
    val searchQuery: String = ""
)

data class ConversationState(
    val title: String,
    val isFavourite: Boolean = false
)