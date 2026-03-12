package com.example.docbot.ui.screens.chat

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docbot.ui.screens.chat.components.DocumentPicker
import com.example.docbot.ui.screens.chat.components.MessageList
import com.example.docbot.ui.screens.chat.components.MessageTextBox
import com.example.docbot.ui.screens.chat.components.UpdateConversationTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    viewModel: ChatViewModel = hiltViewModel<ChatViewModel, ChatViewModelFactory>{
        factory -> factory.create(conversationId)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            viewModel.processDocument(uri)
        }
    }

    fun openFilePickerActivity() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        pdfLauncher.launch(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleUpdateConversationTitleDialog(true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Title",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleDocumentPickerDialog(true) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Add PDFs",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = uiState.snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MessageList(
                messages = uiState.messages,
                currentResponse = uiState.currentResponseMessage,
                modifier = Modifier.weight(1f)
            )
            MessageTextBox(
                value = uiState.currentUserMessage,
                onValueChange = { viewModel.updateCurrentMessage(newMessage = it) },
                createMessage = { viewModel.sendMessage() }
            )
        }

        if (uiState.openUpdateConversationTitleDialog) {
            UpdateConversationTitle(
                onDismissRequest = { viewModel.toggleUpdateConversationTitleDialog(false) },
                value = uiState.title,
                onValueChange = {
                    viewModel.updateConversationTitle(newTitle = it)
                }
            )
        }

        if (uiState.openDocumentPickerSheet) {
            DocumentPicker(
                onDismissRequest = {
                    viewModel.toggleDocumentPickerDialog(false)
                },
                openDocumentPicker = { openFilePickerActivity() },
                documentNames = uiState.documentNames
            )
        }

//        if (uiState.documentProcessing) {
//            CircularProgressIndicator(
//                modifier = Modifier.width(64.dp),
//                color = MaterialTheme.colorScheme.secondary,
//                trackColor = MaterialTheme.colorScheme.surfaceVariant,
//            )
//        }
    }
}