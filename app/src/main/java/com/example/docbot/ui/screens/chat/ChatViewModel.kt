package com.example.docbot.ui.screens.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docbot.data.models.ProcessingStatus
import com.example.docbot.data.conversation.ConversationRepository
import com.example.docbot.data.document.DocumentRepository
import com.example.docbot.data.message.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
    @Assisted private val conversationId: Long,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): ViewModel() {

    // using StateFlow means the UI constantly has access to the up-to-date state
    // therefore we don't have to manually/directly pull the data
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        getConversationTitle()
        getMessages()
        getDocumentNames()
        getDocumentProcessingState()
    }

    private fun getMessages() {
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
        // first, add the prompt to the database
        val messageToSend = _uiState.value.currentUserMessage

        messageRepository.createPrompt(conversationId, messageToSend)

        _uiState.update { it.copy(
            currentUserMessage = "",
            modelInference = true
        ) }

        // then, in the coroutine, generate the response
        viewModelScope.launch(defaultDispatcher) {
            var isFirstWord = true

            messageRepository
                .generateResponse(conversationId, messageToSend)
                .onCompletion {
                    val response = _uiState.value.currentResponseMessage.trimEnd()
                    messageRepository.saveResponse(conversationId, response)
                    _uiState.update { it.copy(
                        currentResponseMessage = "",
                        modelResponding = false
                    ) }
                }
                .collect { value ->
                    if (isFirstWord) {
                        _uiState.update { it.copy(modelInference = false, modelResponding = true) }
                        isFirstWord = false
                    }
                    _uiState.update {
                        it.copy(currentResponseMessage = it.currentResponseMessage + value.toString())
                    }
                }
        }
    }

    fun updateCurrentMessage(newMessage: String) {
        _uiState.update { it.copy(currentUserMessage = newMessage) }
    }

    fun toggleDocumentPickerDialog(isOpen: Boolean) {
        _uiState.update { it.copy(openDocumentPickerSheet = isOpen) }
    }

    fun processDocument(uri: Uri?) {
        if (uri != null) {
            _uiState.update { it.copy(openDocumentPickerSheet = false) }
            viewModelScope.launch(defaultDispatcher) {
                val successfulInitialProcess = documentRepository.processDocument(uri, conversationId)
                if (!successfulInitialProcess) {
                    _uiState.update { it.copy(
                        errorMessage = "Something went wrong. Please ensure you have selected no more than 5 documents, and try again."
                    ) }
                }
            }
        }
    }

    private fun getDocumentProcessingState() {
        viewModelScope.launch {
            documentRepository.getDocumentProcessingFlow(conversationId)
                .collect { processingStatuses ->
                    val isProcessing = processingStatuses.any {
                        it == ProcessingStatus.PROCESSING
                    }
                    _uiState.update { it.copy(documentProcessing = isProcessing) }
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

    fun displayBackMessage() {
        _uiState.update { it.copy(
            errorMessage = "Please wait until the model has finished generating its response before navigating back."
        ) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
