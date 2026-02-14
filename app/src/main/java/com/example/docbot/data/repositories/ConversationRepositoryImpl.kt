package com.example.docbot.data.repositories

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource
): ConversationRepository {
    override fun createConversation() {
        conversationLocalDataSource.insertConversation()
    }

    override fun deleteConversation(conversationId: Long) {
        conversationLocalDataSource.manuallyDeleteConversation(conversationId)
    }

    override fun getConversations(): Flow<List<Conversation>> {
        return conversationLocalDataSource.getConversations()
    }

//    override fun updateTitle(conversationId: Long, title: String) {
//        TODO("Not yet implemented")
//    }
//
    override fun toggleFavourite(conversationId: Long, isFavourite: Boolean) {
        if (isFavourite) {
            conversationLocalDataSource.removeConversationFromFavourites(conversationId)
        }
        else {
            conversationLocalDataSource.addConversationToFavourites(conversationId)
        }
    }

}