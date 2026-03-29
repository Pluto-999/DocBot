package com.example.docbot.ui.screens.home

import androidx.compose.material3.SnackbarHostState


data class HomeUiState (
    val conversations: List<ConversationState> = listOf(),
    val filterMenuExpanded: Boolean = false,
    val sortMenuExpanded: Boolean = false,

    // !! new state for the order and filter of the conversation list !!
    val conversationOrder: ConversationOrder = ConversationOrder.DATE_DESC,
    val conversationFilter: ConversationFilter = ConversationFilter.NONE,
    val searchQuery: String = "",

    val errorMessage: String? = null
)

data class ConversationState(
    val id: Long = 0,
    val title: String = "",
    val isFavourite: Boolean = false,
    val date: String = "",
    val deleteSoon: Boolean = false
)

