package com.example.docbot.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.docbot.data.repositories.DocumentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.core.net.toUri

@HiltWorker
class ProcessDocumentExpeditedWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val documentRepository: DocumentRepository
): CoroutineWorker(appContext, workerParams) {

    // idk if needed ??
//    override suspend fun getForegroundInfo(): ForegroundInfo {
//        return super.getForegroundInfo()
//    }

    override suspend fun doWork(): Result {
        val uriString = inputData.getString("uri")
        val uri = uriString?.toUri() ?: return Result.failure()

        val conversationId = inputData.getLong("conversationId", -1)
        if (conversationId < 0) {
            return Result.failure()
        }

        val successfulProcess = documentRepository.processDocumentImpl(uri, conversationId)
        if (!successfulProcess) {
            return Result.failure()
        }

        // needed by docs apparently -- idk if needed ??
//        try {
//            setForeground(getForegroundInfo())
//        }
//        catch (e: ForegroundServiceStartNotAllowedException) {
//            Log.e("setForeground", e.toString())
//        }

        return Result.success()
    }
}