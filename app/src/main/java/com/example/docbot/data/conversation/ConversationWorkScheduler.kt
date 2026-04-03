package com.example.docbot.data.conversation

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.docbot.workers.ConversationDeletionWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationWorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    fun scheduleDeletion() {
        val deleteRequest = PeriodicWorkRequestBuilder<ConversationDeletionWorker>(
            1, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = "deleteOldConversations",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE,
            request = deleteRequest
        )

        // One time request for testing only !!!

//        val request = OneTimeWorkRequestBuilder<ConversationDeletionWorker>()
//            .build()
//
//        workManager.enqueue(request)
    }

    fun cancelDocumentProcessing(uniqueWorkName: String) {
        workManager.cancelUniqueWork(uniqueWorkName)
    }

}