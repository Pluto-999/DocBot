package com.example.docbot.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.docbot.data.repositories.ConversationRepository
import com.example.docbot.data.repositories.DocumentRepository
import com.example.docbot.data.repositories.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
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
    @Assisted private val conversationId: Long
): ViewModel() {

    // using StateFlow means the UI constantly has access to the up-to-date state
    // therefore we don't have to manually/directly pull the data
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        getConversationTitle()
        getMessages()
        getDocumentNames()
    }

    private fun getMessages() {
        // we must use viewModel coroutine otherwise we can't use .collect !
        viewModelScope.launch {
            messageRepository.getMessages(conversationId).collect { messages ->
                val uiMessages: List<MessageState> = messages.map {
                    MessageState(
                        contents = it.contents,
                        messageType = it.messageType
                    )
                }

                _uiState.update { it.copy(messages = uiMessages) }
            }
        }
    }

    private fun getConversationTitle() {
        val title = conversationRepository.getConversationTitle(conversationId) ?: "Untitled"
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
        viewModelScope.launch {
            messageRepository
                .sendMessage(conversationId, _uiState.value.currentUserMessage)
                .onCompletion {
                    val response = _uiState.value.currentResponseMessage
                    messageRepository.saveResponse(conversationId, response)
                    _uiState.update { it.copy(currentResponseMessage = "") }
                }
                .collect { value ->
                    _uiState.update {
                        it.copy(currentResponseMessage = it.currentResponseMessage + value.toString())
                    }
                }
        }
        _uiState.update { it.copy(currentUserMessage = "") }
    }

    fun updateCurrentMessage(newMessage: String) {
        _uiState.update { it.copy(currentUserMessage = newMessage) }
    }

    fun toggleDocumentPickerDialog(isOpen: Boolean) {
        _uiState.update { it.copy(openDocumentPickerSheet = isOpen) }
    }

    fun processDocument(uri: Uri?) {
        if (uri != null) {
            _uiState.update { it.copy(
                documentProcessing = true,
                openDocumentPickerSheet = false
            ) }

            viewModelScope.launch(Dispatchers.Default) {
                val documentStatusFlow = documentRepository.processDocument(uri, conversationId)
                documentStatusFlow.collect { workInfo ->
                    val workState = workInfo?.state
                    if (workState == WorkInfo.State.SUCCEEDED) {
                        _uiState.update { it.copy(documentProcessing = false) }
                    }
                    else if (workState == WorkInfo.State.FAILED) {
                        _uiState.update { it.copy(documentProcessing = false) }
                        _uiState.value.snackbarHostState.showSnackbar(
                            "Something went wrong. Please ensure you have selected no more than 5 documents, and try again."
                        )
                    }
                }
            }
        }
    }

    fun getDocumentNames() {
        viewModelScope.launch {
            documentRepository.getDocumentTitles(conversationId)
                .collect { documentNames ->
                    _uiState.update { it.copy(documentNames = documentNames) }
                }
        }
    }
}
