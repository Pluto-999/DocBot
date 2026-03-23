package com.example.docbot.data.repositories

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import com.example.docbot.workers.ConversationDeletionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource,
    private val documentLocalDataSource: DocumentLocalDataSource,
    @ApplicationContext private val applicationContext: Context
): ConversationRepository {

    private val workManager = WorkManager.getInstance(applicationContext)

    override fun createConversation(): Long {
        val conversationId = conversationLocalDataSource.insertConversation()
        return conversationId
    }

    override fun deleteConversationManually(conversationId: Long) {
        deleteConversation(conversationId)
    }

    override fun deleteOldConversations() {
        val oldConversationIds = conversationLocalDataSource.getOldConversationsId()

        for (id in oldConversationIds) {
            deleteConversation(id)
        }
    }

    private fun deleteConversation(conversationId: Long) {
        // need to cancel the work that is processing the document associated to this conversation

        // get the documents associated with this conversation
        val documents = conversationLocalDataSource.getDocuments(conversationId)

        for (document in documents) {
            // get the conversations that each document is associated with
            val associatedConversations = documentLocalDataSource.getConversationCountForDocument(document)
            // if it is just 1 conversation, that means it is the current conversation !!!
            // therefore, can cancel the work
            if (associatedConversations == 1) {
                workManager.cancelUniqueWork(document.contentHash)
            }
        }
        conversationLocalDataSource.deleteConversation(conversationId)
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

    override fun scheduleOldConversationsDeletion() {
        val deleteRequest = PeriodicWorkRequestBuilder<ConversationDeletionWorker>(
            1, TimeUnit.HOURS
        ).build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                uniqueWorkName = "deleteOldConversations",
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE,
                request = deleteRequest
            )

        // One time request for testing only !!!

//        val request = OneTimeWorkRequestBuilder<ConversationDeletionWorker>()
//            .build()
//
//        WorkManager.getInstance(applicationContext).enqueue(request)
    }
}