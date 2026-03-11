package com.example.docbot.data.sources

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Message
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
import java.util.concurrent.TimeUnit
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


    /*** Updating Conversations ***/

    fun updateConversationTitle(conversationId: Long, title: String) {
        val conversation: Conversation = conversationBox.get(conversationId)
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


    /*** Deleting Conversations ***/

    fun manuallyDeleteConversation(conversationId: Long) {
        deleteConversation(conversationId)
    }

    fun delete10DayOldConversations() {
        val deleteConversations =
            PeriodicWorkRequestBuilder<ConversationDeletionWorker>(
                1, TimeUnit.DAYS
            ).build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(
            "deleteConversations",
            ExistingPeriodicWorkPolicy.KEEP,
            deleteConversations
        )
    }

    private fun deleteConversation(conversationId: Long) {

        val conversation = conversationBox.get(conversationId)

        // delete related messages
        val messages = conversation.messages.toList()
        for (message in messages) {
            messageBox.remove(message.id)
        }

        // delete related documents and document chunks if relevant
        val documents = conversation.documents.toList()
        for (document in documents) {
            if (document.conversations.size == 1) {
                val chunks = document.documentChunks.toList()
                for (chunk in chunks) {
                    documentChunkBox.remove(chunk.id)
                }
                documentBox.remove(document.id)
            }
        }

        conversationBox.remove(conversationId)
    }
}

@HiltWorker
class ConversationDeletionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val conversationBox: Box<Conversation>,
    private val deleteConversation: (Long) -> Unit,
) : Worker(
    appContext,
    workerParams
) {
    override fun doWork(): Result {
        return Result.success()
    }

    private fun findAndDeleteAll10DayOldConversations() {
        val tenDaysAgo = LocalDateTime.now().minusDays(10)
        // from: https://stackoverflow.com/questions/19431234/converting-between-java-time-localdatetime-and-java-util-date
        val tenDaysAgoDateFormat =
            Date.from(tenDaysAgo.atZone(ZoneId.systemDefault()).toInstant())

        val oldConversationsList = conversationBox
            .query(
                (Conversation_.latestMessage less (tenDaysAgoDateFormat)) and
                        (Conversation_.favourite equal false)
            )
            .build()
            .find()

        for (conversation in oldConversationsList) {
            deleteConversation(conversation.id)
        }
    }
}