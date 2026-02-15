package com.example.docbot.data.repositories

import com.example.docbot.data.models.Conversation
import com.example.docbot.ui.screens.home.GetConversationType
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun createConversation()
    fun deleteConversation(conversationId: Long)
    fun getConversations(type: GetConversationType): Flow<List<Conversation>>
//    fun updateTitle(conversationId: Long, title: String)
    fun toggleFavourite(conversationId: Long, isFavourite: Boolean): Boolean

}