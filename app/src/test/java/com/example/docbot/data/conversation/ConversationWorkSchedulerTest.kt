package com.example.docbot.data.conversation

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ConversationWorkSchedulerTest {

    private lateinit var workManagerMock: WorkManager
    private lateinit var scheduler: ConversationWorkScheduler

    @Before
    fun setUp() {
        workManagerMock = mockk<WorkManager>(relaxed = true)
        scheduler = ConversationWorkScheduler(workManagerMock)
    }


    /****/

    @Test
    fun testScheduleDeletionEnqueuesPeriodicWork() {
        scheduler.scheduleDeletion()

        verify {
            workManagerMock.enqueueUniquePeriodicWork(
                uniqueWorkName = "deleteOldConversations",
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE,
                request = any()
            )
        }
    }


    /****/

    @Test
    fun testCancelDocumentProcessingCancelsCorrectWork() {
        scheduler.cancelDocumentProcessing("test")
        verify { workManagerMock.cancelUniqueWork("test") }
    }
}