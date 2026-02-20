package com.example.docbot.data.repositories

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.ui.screens.home.GetConversationType
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

    override fun getConversations(type: GetConversationType): Flow<List<Conversation>> {
        return when (type) {
            GetConversationType.DATE_ASC -> conversationLocalDataSource.getConversationsDateAscending()
            GetConversationType.DATE_DESC -> conversationLocalDataSource.getConversationsDateDescending()
            GetConversationType.TITLE_ASC -> conversationLocalDataSource.getConversationsAlphabeticallyAscending()
            GetConversationType.TITLE_DESC -> conversationLocalDataSource.getConversationsAlphabeticallyDescending()
            GetConversationType.FAVOURITES -> conversationLocalDataSource.getFavouriteConversations()
            GetConversationType.DELETE_SOON -> conversationLocalDataSource.getSoonToBeDeletedConversations()
            GetConversationType.NONE -> conversationLocalDataSource.getConversations()
        }
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