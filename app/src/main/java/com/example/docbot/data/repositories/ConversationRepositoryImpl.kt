package com.example.docbot.data.repositories

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
        filter: ConversationFilter
    ): Flow<List<Conversation>> {
        val orderedConversations = when (order) {
            ConversationOrder.DATE_ASC -> conversationLocalDataSource.getConversationsDateAscending()
            ConversationOrder.DATE_DESC -> conversationLocalDataSource.getConversationsDateDescending()
            ConversationOrder.TITLE_ASC -> conversationLocalDataSource.getConversationsAlphabeticallyAscending()
            ConversationOrder.TITLE_DESC -> conversationLocalDataSource.getConversationsAlphabeticallyDescending()
        }

        val filteredConversations = when (filter) {
            ConversationFilter.FAVOURITES -> {}
            ConversationFilter.DELETE_SOON -> {}
            ConversationFilter.NONE -> orderedConversations
        }
        return orderedConversations
    }

//    override fun searchForConversation(query: String): Flow<List<Conversation>> {
//        return conversationLocalDataSource.searchForConversation(query)
//    }

    override fun updateTitle(conversationId: Long, title: String) {
        conversationLocalDataSource.updateConversationTitle(conversationId, title)
    }
//

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
                return false
            }
        }
        return true
    }

    override fun getConversationTitle(id: Long): String? {
        return conversationLocalDataSource.getConversationTitleFromId(id)
    }
}