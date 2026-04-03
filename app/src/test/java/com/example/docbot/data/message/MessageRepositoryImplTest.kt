package com.example.docbot.data.message

import com.example.docbot.data.document.DocumentChunkLocalDataSource
import com.example.docbot.data.document.DocumentLocalDataSource
import com.example.docbot.data.message.generation.MessageProcessor
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessageRepositoryImplTest {
    lateinit var messageLocalDataSourceMock: MessageLocalDataSource
    lateinit var documentLocalDataSourceMock: DocumentLocalDataSource
    lateinit var documentChunkLocalDataSourceMock: DocumentChunkLocalDataSource
    lateinit var messageProcessorMock: MessageProcessor

    lateinit var repository: MessageRepository

    @Before
    fun setUp() {
        messageLocalDataSourceMock = mockk<MessageLocalDataSource>()
        documentLocalDataSourceMock = mockk<DocumentLocalDataSource>()
        documentChunkLocalDataSourceMock = mockk<DocumentChunkLocalDataSource>()
        messageProcessorMock = mockk<MessageProcessor>()

        repository = MessageRepositoryImpl(
            messageLocalDataSourceMock,
            documentLocalDataSourceMock,
            documentChunkLocalDataSourceMock,
            messageProcessorMock
        )
    }


    /****/

    @Test
    fun testGetMessagesDelegatesToDataSource() = runTest {
        val messages = listOf(
            Message(contents = "test")
        )
        every { messageLocalDataSourceMock.getMessages(1L) } returns flowOf(messages)

        val result = repository.getMessages(1L).first()

        assertEquals(1, result.size)
        assertEquals("test", result[0].contents)
    }


    /****/

    @Test
    fun testCreatePromptInsertsMessageWithPromptType() {
        every {
            messageLocalDataSourceMock.insertMessage(1, "test", MessageType.PROMPT)
        } just runs

        repository.createPrompt(1, "test")

        verify { messageLocalDataSourceMock.insertMessage(1, "test", MessageType.PROMPT) }
    }


    /****/

    @Test
    fun testGenerateResponseBuildsFullPromptAndDelegatesToGenerator() = runTest {
        val promptEmbedding = com.google.common.collect.ImmutableList.of(1.0f, 2.0f)
        val documentIds = listOf<Long>(1, 2)
        val promptContext = listOf("chunk1", "chunk2")
        val previousMessages = listOf(Message(contents = "test", messageType = MessageType.PROMPT))
        val responseFlow = flowOf(com.google.ai.edge.litertlm.Message.of("response"))

        coEvery { messageProcessorMock.generatePromptEmbedding("test message") } returns promptEmbedding
        every { documentLocalDataSourceMock.getDocumentIds(1) } returns documentIds
        every { documentChunkLocalDataSourceMock.getRelevantChunks(documentIds, promptEmbedding) } returns promptContext
        every { messageLocalDataSourceMock.getRecentMessages(1) } returns previousMessages
        every { messageProcessorMock.generateResponse(promptContext, previousMessages, "test message") } returns responseFlow

        val result = repository.generateResponse(1, "test message")

        assertEquals(responseFlow, result)
        verify { messageProcessorMock.generateResponse(promptContext, previousMessages, "test message") }
    }

    @Test
    fun testGenerateResponseWithNoDocumentsAndNoPreviousMessages() = runTest {
        val promptEmbedding = com.google.common.collect.ImmutableList.of(1.0f)
        val responseFlow = flowOf(
            com.google.ai.edge.litertlm.Message.of("response")
        )

        coEvery { messageProcessorMock.generatePromptEmbedding("test") } returns promptEmbedding
        every { documentLocalDataSourceMock.getDocumentIds(1) } returns emptyList()
        every { documentChunkLocalDataSourceMock.getRelevantChunks(emptyList(), promptEmbedding) } returns emptyList()
        every { messageLocalDataSourceMock.getRecentMessages(1) } returns emptyList()
        every { messageProcessorMock.generateResponse(emptyList(), emptyList(), "test") } returns responseFlow

        val result = repository.generateResponse(1L, "test")

        assertEquals(responseFlow, result)
    }


    /****/

    @Test
    fun testSaveResponseInsertsMessageWithResponseType() {
        every {
            messageLocalDataSourceMock.insertMessage(1, "test", MessageType.RESPONSE)
        } just runs

        repository.saveResponse(1, "test")

        verify { messageLocalDataSourceMock.insertMessage(1, "test", MessageType.RESPONSE) }
    }

}