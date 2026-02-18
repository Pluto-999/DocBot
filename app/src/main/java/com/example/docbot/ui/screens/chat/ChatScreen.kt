package com.example.docbot.ui.screens.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ChatScreen(
    conversationId: Long,
    viewModel: ChatViewModel = hiltViewModel<ChatViewModel, ChatViewModelFactory>{
        factory -> factory.create(conversationId)
    }
) {
    Text(
        "$conversationId",
        modifier = Modifier.padding(20.dp)
    )
}