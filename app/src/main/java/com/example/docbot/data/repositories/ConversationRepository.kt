package com.example.docbot.data.repositories

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun updateTitle(conversationId: Long, title: String)
    fun toggleFavourite(conversationId: Long, isFavourite: Boolean)

}