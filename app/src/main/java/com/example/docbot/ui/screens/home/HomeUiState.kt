package com.example.docbot.ui.screens.home

enum class SortType {
    ASC, DESC
}

data class HomeUiState (
    val conversations: List<ConversationState> = listOf(),
    val searchQuery: String = "",
    val filterMenuExpanded: Boolean = false,
    val sortMenuExpanded: Boolean = false,
    val titleSort: SortType = SortType.ASC,
    val dateSort: SortType = SortType.ASC
)

data class ConversationState(
    val id: Long = 0,
    val title: String = "",
    val isFavourite: Boolean = false,
    val date: String = "",
    val deleteSoon: Boolean = true
)

