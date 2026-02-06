package com.example.docbot.ui.screens.home

data class HomeUiState (
    val conversations: List<ConversationState> = listOf()
)

data class ConversationState(
    val title: String,
    val isFavourite: Boolean = false
)