package com.example.docbot.data.repositories

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource
): ConversationRepository {
    override fun createConversation() {
        TODO("Not yet implemented")
    }

    override fun deleteConversation(conversationId: Long) {
        TODO("Not yet implemented")
    }

    override fun getConversations(): Flow<List<Conversation>> {
        TODO("Not yet implemented")
    }

    override fun updateTitle(conversationId: Long, title: String) {
        TODO("Not yet implemented")
    }

    override fun toggleFavourite(conversationId: Long, isFavourite: Boolean) {
        TODO("Not yet implemented")
    }

}