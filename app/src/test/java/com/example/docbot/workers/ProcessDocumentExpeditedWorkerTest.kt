package com.example.docbot.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.docbot.ui.screens.FakeDocumentRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ProcessDocumentExpeditedWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var fakeDocumentRepository: FakeDocumentRepository

    private lateinit var worker: ProcessDocumentExpeditedWorker

    private lateinit var tempFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        workerParams = mockk<WorkerParameters>(relaxed = true)
        fakeDocumentRepository = FakeDocumentRepository()

        tempFile = File.createTempFile("test", ".txt").apply {
            writeText("document contents")
        }
        every { workerParams.inputData } returns workDataOf(
            "tempFilePath" to tempFile.absolutePath,
            "documentId" to 1L
        )

        worker = ProcessDocumentExpeditedWorker(
            context,
            workerParams,
            fakeDocumentRepository
        )
    }


    /****/

    @Test
    fun testDoWorkReturnsFailureWhenNoFilePathIsProvided() = runTest {
        every { workerParams.inputData } returns workDataOf()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun testDoWorkReturnsFailureWhenNoDocumentIdIsProvided() = runTest {
        every { workerParams.inputData } returns workDataOf(
            "tempFilePath" to tempFile.absolutePath
        )

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun testDoWorkReturnsFailureAndSetsProcessingStatusToFailureWhenProcessChunksThrowsException() = runTest {
        fakeDocumentRepository.throwExceptionOnChunk = true

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)
    }


    /****/

    @Test
    fun testDoWorkProcessesChunksAndReturnsSuccess() = runTest {
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }


    /****/

    @Test
    fun testDoWorkDeletesTempFileAfterProcessing() = runTest {
        worker.doWork()
        assertFalse(tempFile.exists())
    }
}