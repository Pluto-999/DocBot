package com.example.docbot.data.repositories

import com.example.docbot.data.models.Conversation
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun createConversation(): Long
    fun deleteConversation(conversationId: Long)
    fun getConversations(
        order: ConversationOrder,
        filter: ConversationFilter,
        searchQuery: String
    ): Flow<List<Conversation>>
    fun getConversationTitle(id: Long): String?
    fun updateTitle(conversationId: Long, title: String)
    fun toggleFavourite(conversationId: Long, isFavourite: Boolean): Boolean
    fun scheduleOldConversationsDeletion()
}