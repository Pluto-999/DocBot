package com.example.docbot.data.document.processing

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.docbot.workers.ProcessDocumentExpeditedWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class DocumentProcessingScheduler @Inject constructor(
    private val workManager: WorkManager,
    @ApplicationContext private val applicationContext: Context
) {

    fun scheduleProcessing(
        contentsHash: String,
        extractedText: String,
        documentId: Long,
        conversationId: Long
    ) {

        val tempFilePath = createTemporaryFile(
            contentsHash,
            extractedText
        )

        val processRequest = OneTimeWorkRequestBuilder<ProcessDocumentExpeditedWorker>()
            .setInputData(
                workDataOf(
                    "tempFilePath" to tempFilePath,
                    "documentId" to documentId
                )
            )
            .addTag(conversationId.toString())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = contentsHash,
            existingWorkPolicy = ExistingWorkPolicy.KEEP, // keep existing work and ignore new work if duplicate
            request = processRequest
        )
    }

    private fun createTemporaryFile(
        contentsHash: String,
        extractedText: String
    ): String {
        val tempFile = File(applicationContext.cacheDir, "$contentsHash.txt")
        tempFile.writeText(extractedText)
        return tempFile.absolutePath
    }

}