package com.example.docbot.data.sources

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import com.example.docbot.data.models.Document
import com.example.docbot.data.models.DocumentChunk
import com.example.docbot.data.models.Document_
import com.example.docbot.data.models.Message
import com.google.common.util.concurrent.ListenableFuture
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationLocalDataSource @Inject constructor(
    val conversationBox: Box<Conversation>,
    val messageBox: Box<Message>,
    val documentBox: Box<Document>,
    val documentChunkBox: Box<DocumentChunk>
) {

    fun insertConversation() {
        conversationBox.put(Conversation())
    }

    /*** Getting Conversations ***/

    fun getConversations(): Flow<List<Conversation>> {
        return conversationBox
            .query()
            .build()
            .subscribe()
            .toFlow()
    }


    // Sorting Conversations

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


    // Filtering Conversations

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
        val sevenDaysAgoDateFormat =
            Date.from(sevenDaysAgo.atZone(ZoneId.systemDefault()).toInstant())
        return conversationBox
            .query(
                (Conversation_.latestMessage less (sevenDaysAgoDateFormat)) and
                        (Conversation_.favourite equal false)
            )
            .build()
            .subscribe()
            .toFlow()
    }

    // Searching Conversations

    fun searchForConversation(search: String): Flow<List<Conversation>> {
        return conversationBox
            .query(Conversation_.title equal search)
            .build()
            .subscribe()
            .toFlow()
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