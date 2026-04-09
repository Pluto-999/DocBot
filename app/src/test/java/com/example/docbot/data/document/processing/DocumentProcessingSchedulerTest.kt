package com.example.docbot.data.document.processing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DocumentProcessingSchedulerTest {

    private lateinit var workManagerMock: WorkManager
    private lateinit var context: Context
    private lateinit var scheduler: DocumentProcessingScheduler

    @Before
    fun setUp() {
        workManagerMock = mockk<WorkManager>(relaxed = true)
        context = ApplicationProvider.getApplicationContext<Context>()
        scheduler = DocumentProcessingScheduler(workManagerMock, context)
    }


    /****/

    @Test
    fun testDocumentProcessingSchedulerEnqueuesWorkWithHashName() {
        scheduler.scheduleProcessing("hash", "extracted text", 1, 1)

        verify {
            workManagerMock.enqueueUniqueWork(
                uniqueWorkName = "hash",
                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                request = any()
            )
        }
    }

    @Test
    fun testDocumentProcessingSchedulerCreatesTemporaryFileWithExtractedText() {
        scheduler.scheduleProcessing("hash", "extracted text", 1, 2)

        val tempFile = File(context.cacheDir, "hash.txt")
        assert(tempFile.exists())
        assertEquals("extracted text", tempFile.readText())
    }

}