package com.example.docbot.ui.screens.home

import kotlinx.coroutines.flow.StateFlow

interface HomeViewModel {
    val uiState: StateFlow<HomeUiState>

    fun createConversation(): Long
    fun deleteConversation(conversationId: Long)
    fun toggleFavourite(conversationId: Long, isFavourite: Boolean)
    fun clearErrorMessage()
    fun toggleFilterMenu()
    fun collapseFilterMenu()
    fun toggleSortMenu()
    fun collapseSortMenu()
    fun updateTitleOrder()
    fun updateDateOrder()
    fun filterConversations(filter: ConversationFilter)
    fun updateSearchQuery(newQuery: String)
}