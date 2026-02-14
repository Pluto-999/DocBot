package com.example.docbot.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docbot.data.repositories.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // collecting the conversations flow and transforming this into ui specific data (i.e. the state)
    // since this flow gives us list of the conversation data class in data layer
    // but we want this to turn into list of ConversationState
    private fun getConversations() {
        viewModelScope.launch {
            conversationRepository.getConversations().collect { conversations ->
                val uiConversations = mutableListOf<ConversationState>()
                val currentDateTime = LocalDateTime.now()
                for (conversation in conversations) {

                    val latestMessageDateTime = conversation.latestMessage

                    val daysDiff = Duration.between(latestMessageDateTime, currentDateTime).toDays()
                    val hoursDiff = Duration.between(latestMessageDateTime, currentDateTime).toHours()
                    val minutesDiff = Duration.between(latestMessageDateTime, currentDateTime).toMinutes()

                    // calculate difference in hours between current time and latest message
                    var oldConversation = false
                    if (Duration.between(latestMessageDateTime, currentDateTime).toHours() >= 24 * 7) {
                        oldConversation = true
                    }

                    val formattedDate = formateDate(
                        days = daysDiff,
                        hours = hoursDiff,
                        minutes = minutesDiff
                    )

                    uiConversations.add(
                        ConversationState(
                            title = conversation.title,
                            isFavourite = conversation.favourite,
                            date = formattedDate,
                            deleteSoon = oldConversation
                        )
                    )
                }
                _uiState.update { it.copy(conversations = uiConversations) }
            }
        }
    }

    fun formateDate(days: Long, hours: Long, minutes: Long): String {

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

    fun updateSearchQuery(newQuery: String) {
        _uiState.update{ it.copy(searchQuery = newQuery) }
    }

    fun toggleFilterMenu(newState: Boolean) {
        _uiState.update { it.copy(filterMenuExpanded = newState) }
    }

    fun toggleSortMenu(newState: Boolean) {
        _uiState.update { it.copy(sortMenuExpanded = newState) }
    }

    fun updateTitleSort(newTitleSort: SortType) {
        _uiState.update { it.copy(titleSort = newTitleSort) }
    }

    fun updateDateSort(newDateSort: SortType) {
        _uiState.update { it.copy(dateSort = newDateSort) }
    }
}