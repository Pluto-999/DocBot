package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Message
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import io.objectbox.Box
import io.objectbox.kotlin.and
import io.objectbox.kotlin.equal
import io.objectbox.kotlin.less
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class ConversationLocalDataSource @Inject constructor(
    private val conversationBox: Box<Conversation>,
    private val messageBox: Box<Message>,
    private val documentBox: Box<Document>,
    private val documentChunkBox: Box<DocumentChunk>
) {

    fun insertConversation(): Long {
        val conversationId = conversationBox.put(Conversation())
        return conversationId
    }


    /*** Getting Conversations ***/

    fun getConversations(
        order: ConversationOrder,
        filter: ConversationFilter,
        searchQuery: String
    ): Flow<List<Conversation>> {
        val builder = createConversationBuilder(filter, searchQuery)

        return createConversationFlow(builder, order)
    }

    private fun createConversationBuilder(filter: ConversationFilter, searchQuery: String): QueryBuilder<Conversation> {
        val builder =  when (filter) {
            ConversationFilter.FAVOURITES ->
                conversationBox.query(
                    Conversation_.favourite.equal(true) and
                    Conversation_.title.startsWith(searchQuery, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                )
            ConversationFilter.DELETE_SOON -> {
                conversationBox.query(
                    Conversation_.latestMessage.less(getSevenDaysAgoDate()) and
                    Conversation_.favourite.equal(false) and
                    Conversation_.title.startsWith(searchQuery, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                )
            }
            ConversationFilter.NONE ->
                conversationBox.query(
                    Conversation_.title.startsWith(searchQuery, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                )
        }
        return builder
    }

    private fun getSevenDaysAgoDate(): Date {
        val sevenDaysAgo = LocalDateTime.now().minusDays(7)
        // from: https://stackoverflow.com/questions/19431234/converting-between-java-time-localdatetime-and-java-util-date
        return Date.from(sevenDaysAgo.atZone(ZoneId.systemDefault()).toInstant())
    }

    private fun createConversationFlow(builder: QueryBuilder<Conversation>, order: ConversationOrder): Flow<List<Conversation>> {
        return when (order) {
            ConversationOrder.DATE_ASC ->
                builder.order(Conversation_.latestMessage).build().subscribe().toFlow()
            ConversationOrder.DATE_DESC ->
                builder.order(Conversation_.latestMessage, QueryBuilder.DESCENDING).build().subscribe().toFlow()
            ConversationOrder.TITLE_ASC ->
                builder.order(Conversation_.title).build().subscribe().toFlow()
            ConversationOrder.TITLE_DESC ->
                builder.order(Conversation_.title, QueryBuilder.DESCENDING).build().subscribe().toFlow()
        }
    }

    fun getConversationTitleFromId(id: Long): String? {
        val conversation = conversationBox
            .query(Conversation_.id equal id)
            .build()
            .findUnique()
        return conversation?.title
    }

    /** Document Related Stuff **/

    // for UI to display
    fun getDocumentTitlesFromId(conversationId: Long): Flow<List<String>> {
        return conversationBox
            .query(Conversation_.id.equal(conversationId))
            .build()
            .subscribe()
            .toFlow()
            .map { conversations ->
                conversations.flatMap { conversation ->
                    conversation.documents.map { it.name }
                }
            }
        // first map is for the Flow - transforms each emission
        // second map (flatMap) is to flatten List<ToMany<Document>> to List<Document>
        // third map is for each document, just getting its name
    }

    fun getDocumentCount(conversationId: Long): Int {
        val conversation = conversationBox.get(conversationId) ?: return -1
        return conversation.documents.size
    }


    /*** Updating Conversations ***/

    fun updateConversationTitle(conversationId: Long, title: String) {
        val conversation = conversationBox.get(conversationId) ?: return
        conversation.title = title
        conversationBox.put(conversation)
    }

    fun addConversationToFavourites(conversationId: Long) {
        val favouriteCount = conversationBox
            .query(Conversation_.favourite.equal(true))
            .build()
            .count()
        if (favouriteCount >= 10) {
            throw IllegalStateException("Maximum of 10 favourites reached")
        } else {
            val conversation = conversationBox.get(conversationId) ?: return
            conversation.favourite = true
            conversationBox.put(conversation)
        }
    }

    fun removeConversationFromFavourites(conversationId: Long) {
        val conversation = conversationBox.get(conversationId) ?: return
        conversation.favourite = false
        conversationBox.put(conversation)
    }


    /*** Deleting Conversations ***/

    fun getOldConversationsId(): List<Long> {
        val tenDaysAgo = LocalDateTime.now().minusDays(10)
        // from: https://stackoverflow.com/questions/19431234/converting-between-java-time-localdatetime-and-java-util-date
        val tenDaysAgoDateFormat = Date.from(tenDaysAgo.atZone(ZoneId.systemDefault()).toInstant())

        val oldConversationsList = conversationBox
            .query(
                (Conversation_.latestMessage less (tenDaysAgoDateFormat)) and
                        (Conversation_.favourite equal false)
            )
            .build()
            .find()

        return oldConversationsList.map { it.id }
    }

    fun deleteConversation(conversationId: Long) {
        conversationBox.store.callInTx {
            val conversation = conversationBox.get(conversationId) ?: return@callInTx

            // delete related messages
            val messages = conversation.messages.toList()
            messageBox.remove(messages)

            // delete related documents and document chunks if relevant
            val documents = getDocuments(conversationId)
            for (document in documents) {
                if (document.conversations.size == 1) {
                    // delete all chunks
                    val chunks = document.documentChunks.toList()
                    documentChunkBox.remove(chunks)

                    // delete the document
                    documentBox.remove(document.id)
                }
            }
            // finally, delete the conversation
            conversationBox.remove(conversationId)
        }
    }

    fun getDocuments(conversationId: Long): List<Document> {
        val conversation = conversationBox.get(conversationId) ?: return emptyList()
        val documents = conversation.documents.toList()
        return documents
    }

}