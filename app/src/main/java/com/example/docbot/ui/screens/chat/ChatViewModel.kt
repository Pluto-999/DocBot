package com.example.docbot.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docbot.data.repositories.ConversationRepository
import com.example.docbot.data.repositories.DocumentRepository
import com.example.docbot.data.repositories.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@AssistedFactory
interface ChatViewModelFactory {
    fun create(conversationId: Long): ChatViewModel
}

@HiltViewModel(assistedFactory = ChatViewModelFactory::class)
class ChatViewModel @AssistedInject constructor(
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val documentRepository: DocumentRepository,
    @Assisted val conversationId: Long
): ViewModel() {

    // using StateFlow means the UI constantly has access to the up-to-date state
    // therefore we don't have to manually/directly pull the data
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        getConversationTitle()
        getMessages()
    }

    private fun getMessages() {
        // we must use viewModel coroutine otherwise we can't use .collect !
        viewModelScope.launch {
            messageRepository.getMessages(conversationId).collect { messages ->
                val uiMessages = mutableListOf<MessageState>()
                for (message in messages) {
                    uiMessages.add(
                        MessageState(
                            contents = message.contents,
                            messageType = message.messageType
                        )
                    )
                }
                _uiState.update { it.copy(messages = uiMessages) }
            }
        }
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

    fun sendMessage() {
        messageRepository.sendMessage(conversationId, _uiState.value.currentMessage)
        _uiState.update { it.copy(currentMessage = "") }
    }

    fun updateCurrentMessage(newMessage: String) {
        _uiState.update { it.copy(currentMessage = newMessage) }
    }

    fun processDocument(uri: Uri?) {
        if (uri != null) {
            documentRepository.processPDF(uri)
        }
    }
}
