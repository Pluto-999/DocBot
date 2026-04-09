package com.example.docbot.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.docbot.ui.screens.home.ConversationState

@Composable
fun ConversationList(
    conversations: List<ConversationState>,
    onConversationFavouriteClick: (conversationId: Long, isFavourite: Boolean) -> Unit,
    onConversationDeleteClick: (conversationId: Long) -> Unit,
    onConversationClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(conversations) { conversation ->
            ConversationCard(
                title = conversation.title,
                date = conversation.date,
                isFavourite = conversation.isFavourite,
                openConversation = { onConversationClick(conversation.id) },
                favouriteClick = { onConversationFavouriteClick(conversation.id, conversation.isFavourite) },
                deleteClick = { onConversationDeleteClick(conversation.id) }
            )
        }
    }
}