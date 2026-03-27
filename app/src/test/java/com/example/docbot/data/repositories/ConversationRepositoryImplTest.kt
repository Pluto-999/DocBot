package com.example.docbot.data.repositories

import androidx.work.WorkManager
import com.example.docbot.data.sources.ConversationLocalDataSource
import com.example.docbot.data.sources.DocumentLocalDataSource
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class ConversationRepositoryImplTest {

    lateinit var conversationLocalDataSourceMock: ConversationLocalDataSource
    lateinit var documentLocalDataSourceMock: DocumentLocalDataSource
    lateinit var workManagerMock: WorkManager

    lateinit var repository: ConversationRepositoryImpl

    @Before
    fun setUp() {
        conversationLocalDataSourceMock = mockk<ConversationLocalDataSource>()
        documentLocalDataSourceMock = mockk<DocumentLocalDataSource>()
        workManagerMock = mockk<WorkManager>()

        repository = ConversationRepositoryImpl(
            conversationLocalDataSourceMock,
            documentLocalDataSourceMock,
            workManagerMock
        )
    }

    @Test
    fun testCreateConversationReturnsIdFromLocalDataSource() {
        every { conversationLocalDataSourceMock.insertConversation() } returns 1
        val conversationId = repository.createConversation()
        assertEquals(1, conversationId)
    }


//    @Test
//    fun testGetConversationsReturns

}