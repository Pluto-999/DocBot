package com.example.docbot.ui.screens.chat

import androidx.lifecycle.ViewModel
import com.example.docbot.data.repositories.ConversationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


@AssistedFactory
interface ChatViewModelFactory {
    fun create(conversationId: Long): ChatViewModel
}

@HiltViewModel(assistedFactory = ChatViewModelFactory::class)
class ChatViewModel @AssistedInject constructor(
    private val conversationRepository: ConversationRepository,
    @Assisted val conversationId: Long
): ViewModel() {

    // using StateFlow means the UI constantly has access to the up-to-date state
    // therefore we don't have to manually/directly pull the data
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        getConversationTitle()
    }

    private fun getConversationTitle() {
        val title = conversationRepository.getConversationTitle(conversationId) ?: "Blah"
        _uiState.update { it.copy(title = title) }
    }

    fun toggleUpdateConversationTitleDialog(isOpen: Boolean) {
        _uiState.update { it.copy(openUpdateConversationTitleDialog = isOpen) }
    }

    fun updateConversationTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        conversationRepository.updateTitle(conversationId, newTitle)
    }

}
