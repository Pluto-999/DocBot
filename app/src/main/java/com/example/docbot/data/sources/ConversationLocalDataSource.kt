package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import io.objectbox.Box
import io.objectbox.kotlin.and
import io.objectbox.kotlin.equal
import io.objectbox.kotlin.less
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class ConversationLocalDataSource (
    val conversationBox: Box<Conversation>
){

//    fun insertConversation(conversation: Conversation) {
//        conversationBox.put(conversation)
//
//    }

    fun getConversations(): Flow<List<Conversation>> {
        return conversationBox
            .query()
            .build()
            .subscribe()
            .toFlow()
    }

    fun deleteConversation(conversationId: Long) {
        conversationBox.remove(conversationId)
    }


    /* Updating Conversations */

    fun updateConversationTitle(conversationId: Long, title: String) {
        val conversation: Conversation = conversationBox.get(conversationId)
        conversation.title = title
        conversationBox.put(conversation)
    }

    fun addConversationToFavourites(conversationId: Long)  {
        val favouriteCount = conversationBox
            .query(Conversation_.favourite.equal(true))
            .build()
            .count()
        if (favouriteCount >= 10) {
            throw IllegalStateException("Maximum of 10 favourites reached")
        }
        else {
            val conversation: Conversation = conversationBox.get(conversationId)
            conversation.favourite = true
            conversationBox.put(conversation)
        }
    }

    fun removeConversationFromFavourites(conversationId: Long) {
        val conversation: Conversation = conversationBox.get(conversationId)
        conversation.favourite = false
        conversationBox.put(conversation)
    }


    /* Sorting Conversations */

    fun getConversationsAlphabeticallyAscending(): Flow<List<Conversation>> {
        return conversationBox
            .query()
            .order(Conversation_.title)
            .build()
            .subscribe()
            .toFlow()
    }

    fun getConversationsAlphabeticallyDescending(): Flow<List<Conversation>> {
        return conversationBox
            .query()
            .order(Conversation_.title, QueryBuilder.DESCENDING)
            .build()
            .subscribe()
            .toFlow()
    }

    fun getConversationsDateAscending(): Flow<List<Conversation>> {
        return conversationBox
            .query()
            .order(Conversation_.latestMessage)
            .build()
            .subscribe()
            .toFlow()
    }

    fun getConversationsDateDescending(): Flow<List<Conversation>> {
        return conversationBox
            .query()
            .order(Conversation_.latestMessage, QueryBuilder.DESCENDING)
            .build()
            .subscribe()
            .toFlow()
    }


    /* Filtering Conversations */

    fun getFavouriteConversations(): Flow<List<Conversation>> {
        return conversationBox
            .query(Conversation_.favourite equal true)
            .build()
            .subscribe()
            .toFlow()
    }

    fun getSoonToBeDeletedConversations(): Flow<List<Conversation>> {
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        // from: https://stackoverflow.com/questions/19431234/converting-between-java-time-localdatetime-and-java-util-date
        val sevenDaysAgoDateFormat = Date.from(sevenDaysAgo.atZone(ZoneId.systemDefault()).toInstant())
        return conversationBox
            .query(
                (Conversation_.latestMessage less(sevenDaysAgoDateFormat)) and
                (Conversation_.favourite equal false)
            )
            .build()
            .subscribe()
            .toFlow()
    }


    /* Searching Conversations */

    fun searchForConversation(search: String): Flow<List<Conversation>> {
        return conversationBox
            .query(Conversation_.title equal search)
            .build()
            .subscribe()
            .toFlow()
    }

}

