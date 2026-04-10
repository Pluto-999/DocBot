package com.example.docbot.data.message.generation

import com.example.docbot.data.FakeEmbeddingGenerator
import com.example.docbot.data.models.Message
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MessageProcessorTest {

    private lateinit var fakeEmbeddingGenerator: FakeEmbeddingGenerator
    private lateinit var messageGeneratorMock: MessageGenerator
    private lateinit var promptFormatterMock: PromptFormatter

    private lateinit var messageProcessor: MessageProcessor

    @Before
    fun setUp() {
        fakeEmbeddingGenerator = FakeEmbeddingGenerator()
        messageGeneratorMock = mockk<MessageGenerator>()
        promptFormatterMock = mockk<PromptFormatter>()

        messageProcessor = MessageProcessor(
            fakeEmbeddingGenerator,
            messageGeneratorMock,
            promptFormatterMock
        )
    }


    /****/

    @Test
    fun testGeneratePromptEmbeddingReturnsEmbeddingFromEmbeddingGenerator() = runTest {
        val result = messageProcessor.generatePromptEmbedding("message")

        assertEquals(listOf(1f, 2f), result)
    }


    /****/

    @Test
    fun testGenerateResponseFormatsThePromptContextAndPreviousMessages() {
        val promptContext = listOf("context1", "context2")
        val previousMessages = listOf<Message>()
        val prompt = "prompt"

        every { promptFormatterMock.formatMessageContext(promptContext) } returns ""
        every { promptFormatterMock.formatPreviousMessages(previousMessages) } returns ""
        every { messageGeneratorMock.generateResponse(any()) } returns flowOf()

        messageProcessor.generateResponse(promptContext, previousMessages, prompt)

        verify { promptFormatterMock.formatMessageContext(promptContext) }
        verify { promptFormatterMock.formatPreviousMessages(previousMessages) }
    }

    @Test
    fun testGenerateResponsePassesFormattedFullPromptToTheMessageGenerator() {
        val promptContext = listOf("context1", "context2")
        val previousMessages = listOf<Message>()
        val prompt = "prompt"

        every { promptFormatterMock.formatMessageContext(promptContext) } returns "formatted context"
        every { promptFormatterMock.formatPreviousMessages(previousMessages) } returns "formatted messages"
        every { messageGeneratorMock.generateResponse(any()) } returns flowOf()

        messageProcessor.generateResponse(promptContext, previousMessages, prompt)

        val expectedFullPrompt = """
        Answer the following prompt: $prompt
        ----------------------
        formatted context
        ----------------------
        formatted messages
    """.trimIndent()

        verify { messageGeneratorMock.generateResponse(expectedFullPrompt) }
    }
}