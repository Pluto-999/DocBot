package com.example.docbot.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docbot.data.repositories.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
): ViewModel() {

    // using StateFlow means the UI constantly has access to the up-to-date state
    // therefore we don't have to manually/directly pull the data
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        getConversations()
    }

    fun createConversation() {
        conversationRepository.createConversation()
    }

    fun deleteConversation(conversationId: Long) {
        conversationRepository.deleteConversation(conversationId)
    }

    private var conversationJob: Job? = null

    // collecting the conversations flow and transforming this into ui specific data (i.e. the state)
    // since this flow gives us list of the conversation data class in data layer
    // but we want this to turn into list of ConversationState
    private fun getConversations() {
        conversationJob?.cancel()
        // we must use viewModel coroutine otherwise we can't use .collect !
        conversationJob = viewModelScope.launch {
            conversationRepository.getConversations(
                _uiState.value.conversationOrder,
                _uiState.value.conversationFilter,
                _uiState.value.searchQuery
            ).collect { conversations ->
                val currentDateTime = LocalDateTime.now()

                val uiConversations: List<ConversationState> = conversations.map {
                    val latestMessageDateTime = it.latestMessage

                    val daysDiff = Duration.between(latestMessageDateTime, currentDateTime).toDays()
                    val hoursDiff = Duration.between(latestMessageDateTime, currentDateTime).toHours()
                    val minutesDiff = Duration.between(latestMessageDateTime, currentDateTime).toMinutes()

                    // calculate difference in hours between current time and latest message
                    var oldConversation = false
                    if (Duration.between(latestMessageDateTime, currentDateTime).toHours() >= 24 * 7) {
                        oldConversation = true
                    }

                    val formattedDate = formatDate(
                        days = daysDiff,
                        hours = hoursDiff,
                        minutes = minutesDiff
                    )

                    // create the conversation state
                    ConversationState(
                        id = it.id,
                        title = it.title,
                        isFavourite = it.favourite,
                        date = formattedDate,
                        deleteSoon = oldConversation
                    )
                }

                _uiState.update { it.copy(conversations = uiConversations) }
            }
        }
    }

    private fun formatDate(days: Long, hours: Long, minutes: Long): String {
        if (days == 1.toLong()) {
            return "$days day ago"
        }
        if (days >= 1) {
            return "$days days ago"
        }
        if (hours >= 1) {
            return "$hours hours ago"
        }
        if (minutes < 1) {
            return "Just now"
        }
        return "$minutes minutes ago"
    }

    fun toggleFavourite(conversationId: Long, isFavourite: Boolean) {
        val successfulToggle = conversationRepository.toggleFavourite(conversationId, isFavourite)

        if (!successfulToggle) {
            viewModelScope.launch {
                _uiState.value.snackbarHostState.showSnackbar(
                    "You can only have up to 10 favourite conversations"
                )
            }
        }
    }

    fun toggleFilterMenu(newState: Boolean) {
        _uiState.update { it.copy(filterMenuExpanded = newState) }
    }

    fun toggleSortMenu(newState: Boolean) {
        _uiState.update { it.copy(sortMenuExpanded = newState) }
    }

    fun updateConversationOrder(order: ConversationOrder) {
        _uiState.update { it.copy(conversationOrder = order) }
        getConversations()
    }

    fun updateConversationFilter(filter: ConversationFilter) {
        _uiState.update { it.copy(conversationFilter = filter) }
        getConversations()
    }

    fun updateSearchQuery(newQuery: String) {
        _uiState.update{ it.copy(searchQuery = newQuery) }
        getConversations()
    }
}


enum class ConversationOrder {
    DATE_ASC, DATE_DESC, TITLE_ASC, TITLE_DESC
}

enum class ConversationFilter {
    FAVOURITES, DELETE_SOON, NONE
}