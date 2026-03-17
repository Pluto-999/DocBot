package com.example.docbot.data.repositories

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.docbot.data.models.Conversation
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.ui.screens.home.ConversationFilter
import com.example.docbot.ui.screens.home.ConversationOrder
import com.example.docbot.workers.ConversationDeletionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationLocalDataSource: ConversationLocalDataSource,
    @ApplicationContext private val applicationContext: Context
): ConversationRepository {
    override fun createConversation(): Long {
        val conversationId = conversationLocalDataSource.insertConversation()
        return conversationId
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