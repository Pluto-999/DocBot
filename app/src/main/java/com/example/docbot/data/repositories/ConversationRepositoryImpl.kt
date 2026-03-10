package com.example.docbot.data.repositories

import android.util.Log
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
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

    override fun getConversations(
        order: ConversationOrder,
        filter: ConversationFilter,
        searchQuery: String
    ): Flow<List<Conversation>> {
        return conversationLocalDataSource.getConversations(order, filter, searchQuery)
    }

    override fun updateTitle(conversationId: Long, title: String) {
        conversationLocalDataSource.updateConversationTitle(conversationId, title)
    }

    // returns true if the toggle was successful and false otherwise
    override fun toggleFavourite(conversationId: Long, isFavourite: Boolean): Boolean {
        if (isFavourite) {
            conversationLocalDataSource.removeConversationFromFavourites(conversationId)
        }
        else {
            try {
                conversationLocalDataSource.addConversationToFavourites(conversationId)
            }
            catch (exception: IllegalStateException) {
                Log.e(
                    "toggleFavourite",
                    "Exception thrown in toggleFavourite function: $exception"
                )
                return false
            }
        }
        return true
    }

    override fun getConversationTitle(id: Long): String? {
        return conversationLocalDataSource.getConversationTitleFromId(id)
    }
}