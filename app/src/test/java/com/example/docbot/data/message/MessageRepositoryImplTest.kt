package com.example.docbot.data.message

import com.example.docbot.data.document.DocumentChunkLocalDataSource
import com.example.docbot.data.document.DocumentLocalDataSource
import com.example.docbot.data.embedding.EmbeddingGenerator
import com.example.docbot.data.message.generation.MessageGenerator
import com.example.docbot.data.message.generation.PromptFormatter
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.google.ai.edge.localagents.rag.models.EmbedData
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
    lateinit var embeddingGeneratorMock: EmbeddingGenerator
    lateinit var messageGeneratorMock: MessageGenerator
    lateinit var promptFormatterMock: PromptFormatter

    lateinit var repository: MessageRepository

    @Before
    fun setUp() {
        messageLocalDataSourceMock = mockk<MessageLocalDataSource>()
        documentLocalDataSourceMock = mockk<DocumentLocalDataSource>()
        documentChunkLocalDataSourceMock = mockk<DocumentChunkLocalDataSource>()
        embeddingGeneratorMock = mockk<EmbeddingGenerator>()
        messageGeneratorMock = mockk<MessageGenerator>()
        promptFormatterMock = mockk<PromptFormatter>()

        repository = MessageRepositoryImpl(
            messageLocalDataSourceMock,
            documentLocalDataSourceMock,
            documentChunkLocalDataSourceMock,
            embeddingGeneratorMock,
            messageGeneratorMock,
            promptFormatterMock
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
        val fakeEmbedding = com.google.common.collect.ImmutableList.of(1.0f, 2.0f)
        val fakeDocIds = listOf<Long>(1, 2)
        val fakeChunks = listOf("chunk1", "chunk2")
        val fakeMessages = listOf(
            Message(messageType = MessageType.PROMPT),
            Message(messageType = MessageType.RESPONSE)
        )
        val fakeResponseFlow = flowOf(
            com.google.ai.edge.litertlm.Message.of("response")
        )

        // getFormattedContext
        coEvery {
            embeddingGeneratorMock.generateEmbedding("test message", EmbedData.TaskType.RETRIEVAL_QUERY, true)
        } returns fakeEmbedding
        every { documentLocalDataSourceMock.getDocumentIds(1) } returns fakeDocIds
        every { documentChunkLocalDataSourceMock.getRelevantChunks(fakeDocIds, fakeEmbedding) } returns fakeChunks
        every { promptFormatterMock.formatMessageContext(fakeChunks) } returns "formatted context"

        // getFormattedPreviousMessages
        every { messageLocalDataSourceMock.getRecentMessages(1) } returns fakeMessages
        every { promptFormatterMock.formatPreviousMessages(fakeMessages) } returns "formatted messages"

        every { messageGeneratorMock.generateResponse(any()) } returns fakeResponseFlow

        val result = repository.generateResponse(1, "test message")

        assertEquals(fakeResponseFlow, result)

        verify { messageGeneratorMock.generateResponse(match { prompt ->
            prompt.contains("formatted context") &&
                    prompt.contains("formatted messages") &&
                    prompt.contains("test message")
        })}
    }

    @Test
    fun testGenerateResponseWithNoDocumentsAndNoPreviousMessages() = runTest {
        val fakeEmbedding = com.google.common.collect.ImmutableList.of(1.0f)
        val fakeResponseFlow = flowOf(
            com.google.ai.edge.litertlm.Message.of("response")
        )

        // getFormattedContext
        coEvery {
            embeddingGeneratorMock.generateEmbedding(any(), any(), any())
        } returns fakeEmbedding
        every { documentLocalDataSourceMock.getDocumentIds(1) } returns emptyList()
        every { documentChunkLocalDataSourceMock.getRelevantChunks(emptyList(), fakeEmbedding) } returns emptyList()
        every { promptFormatterMock.formatMessageContext(emptyList()) } returns ""

        // getFormattedPreviousMessages
        every { messageLocalDataSourceMock.getRecentMessages(1) } returns emptyList()
        every { promptFormatterMock.formatPreviousMessages(emptyList()) } returns ""

        every { messageGeneratorMock.generateResponse(any()) } returns fakeResponseFlow

        val result = repository.generateResponse(1, "hello")

        assertEquals(fakeResponseFlow, result)
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