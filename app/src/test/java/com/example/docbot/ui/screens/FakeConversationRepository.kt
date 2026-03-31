package com.example.docbot.ui.screens

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.conversation.ConversationRepository
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

class FakeConversationRepository: ConversationRepository {

    private val conversations = mutableListOf<Conversation>()
    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
    var favouriteToggleSuccess = true
    var returnNullTitle = false

    override fun createConversation(): Long {
        val conversationId = (conversations.size + 1).toLong()
        val conversation = Conversation(id = conversationId)
        conversations.add(conversation)
        conversationsFlow.value = conversations.toList()
        return conversationId
    }

    fun createConversationWithLatestMessage(latestMessage: LocalDateTime): Long {
        val id = (conversations.size + 1).toLong()
        conversations.add(Conversation(id = id, latestMessage = latestMessage))
        conversationsFlow.value = conversations.toList()
        return id
    }

    override fun deleteConversationManually(conversationId: Long) {
        conversations.removeIf { it.id == conversationId }
        conversationsFlow.value = conversations.toList()
    }

    override fun deleteOldConversations() { }

    override fun getConversations(
        order: ConversationOrder,
        filter: ConversationFilter,
        searchQuery: String
    ): Flow<List<Conversation>> {
        return conversationsFlow
    }

    override fun getConversationTitle(conversationId: Long): String? {
        if (returnNullTitle) return null
        return conversations.find { it.id == conversationId }?.title
    }

    override fun updateTitle(conversationId: Long, title: String) {
        val conversation = conversations.find { it.id == conversationId }
        conversation?.title = title
        conversationsFlow.value = conversations.toList()
    }

    override fun toggleFavourite(
        conversationId: Long,
        isFavourite: Boolean
    ): Boolean {
        val conversation = conversations.find { it.id == conversationId }
        conversation?.favourite = !isFavourite
        conversationsFlow.value = conversations.toList()
        return favouriteToggleSuccess
    }

    override fun scheduleOldConversationsDeletion() { }

}