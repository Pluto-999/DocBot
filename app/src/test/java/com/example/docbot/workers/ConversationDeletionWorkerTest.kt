package com.example.docbot.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.docbot.ui.screens.FakeConversationRepository
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConversationDeletionWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var fakeConversationRepository: FakeConversationRepository

    private lateinit var worker: ConversationDeletionWorker

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        workerParams = mockk<WorkerParameters>(relaxed = true)
        fakeConversationRepository = FakeConversationRepository()

        worker = ConversationDeletionWorker(
            context,
            workerParams,
            fakeConversationRepository
        )
    }


    /****/

    @Test
    fun testDoWorkDeletesOldConversationsAndReturnsSuccess() {
        val result = worker.doWork()

        assertTrue(fakeConversationRepository.deleteOldConversationsCalled)
        assertEquals(ListenableWorker.Result.success(), result)
    }
}