package com.example.docbot.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.docbot.data.document.DocumentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.example.docbot.data.models.ProcessingStatus
import java.io.File

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
        val filePath = inputData.getString("tempFilePath") ?: return Result.failure()

        val documentId = inputData.getLong("documentId", -1)
        if (documentId < 0) {
            File(filePath).delete()
            return Result.failure()
        }

        try {
            val documentContents = File(filePath).readText()

            documentRepository.processChunks(documentId, documentContents)

            // now processing is done, update the document processing value
            documentRepository.updateProcessingStatus(documentId, ProcessingStatus.COMPLETED)

            return Result.success()
        }
        catch (e: Exception) {
            documentRepository.updateProcessingStatus(documentId, ProcessingStatus.FAILED)
            return Result.failure()
        }
        finally {
            File(filePath).delete()
        }
    }

    // needed by docs apparently -- idk if needed ??
//        try {
//            setForeground(getForegroundInfo())
//        }
//        catch (e: ForegroundServiceStartNotAllowedException) {
//            Log.e("setForeground", e.toString())
//        }
}