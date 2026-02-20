package com.example.docbot.ui.screens.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                        onClick = { /* click me ! */ }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = "Add PDFs",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Text(
            "$conversationId",
            modifier = Modifier.padding(innerPadding)
        )

        if (uiState.openUpdateConversationTitleDialog) {
            UpdateConversationTitle(
                onDismissRequest = { viewModel.toggleUpdateConversationTitleDialog(false) },
                value = uiState.title,
                onValueChange = {
                    viewModel.updateConversationTitle(newTitle = it)
                }
            )
        }
    }

}