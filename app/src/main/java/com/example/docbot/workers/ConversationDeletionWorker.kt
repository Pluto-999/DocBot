package com.example.docbot.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.docbot.data.repositories.ConversationRepository
import com.example.docbot.data.sources.ConversationLocalDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ConversationDeletionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val conversationRepository: ConversationRepository
): Worker(appContext, workerParams) {

    override fun doWork(): Result {
        conversationRepository.deleteOldConversations()
        return Result.success()
    }
}