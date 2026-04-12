package com.example.docbot.ui.screens.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHomeViewModel: HomeViewModel {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            conversations = listOf(
                ConversationState(id = 1, title = "title 1"),
                ConversationState(id = 2, title = "title 2"),
                ConversationState(id = 3, title = "title 3")
            )
        )
    )
    override val uiState: StateFlow<HomeUiState> = _uiState

    fun setUiState(newState: HomeUiState) {
        _uiState.value = newState
    }

    override fun createConversation(): Long {
        return 100
    }

    var deletedId = -1L
    override fun deleteConversation(conversationId: Long) {
        deletedId = conversationId
    }

    var toggledId = -1L
    override fun toggleFavourite(conversationId: Long, isFavourite: Boolean) {
        toggledId = conversationId
    }

    var errorCleared = false
    override fun clearErrorMessage() {
        errorCleared = true
    }

    var filterMenuToggled = false
    override fun toggleFilterMenu() {
        filterMenuToggled = true
    }

    var filterMenuCollapsed = false
    override fun collapseFilterMenu() {
        filterMenuCollapsed = true
    }

    var sortMenuToggled = false
    override fun toggleSortMenu() {
        sortMenuToggled = true
    }

    var sortMenuCollapsed = false
    override fun collapseSortMenu() {
        sortMenuCollapsed = true
    }

    var titleOrderUpdated = false
    override fun updateTitleOrder() {
        titleOrderUpdated = true
    }

    var dateOrderUpdated = false
    override fun updateDateOrder() {
        dateOrderUpdated = true
    }

    var filterSelected: ConversationFilter? = null
    override fun filterConversations(filter: ConversationFilter) {
        filterSelected = filter
    }

    var searchQuery = ""
    override fun updateSearchQuery(newQuery: String) {
        searchQuery = newQuery
    }
}