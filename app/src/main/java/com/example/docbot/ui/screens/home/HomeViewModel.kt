package com.example.docbot.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.docbot.data.repositories.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository
): ViewModel() {

    // using StateFlow means the UI constantly has access to the up-to-date state
    // don't have to manually/directly pull the data
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


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