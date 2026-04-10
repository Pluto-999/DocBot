package com.example.docbot.data.message.generation

import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MessageGeneratorTest {

    lateinit var engineMock: Engine
    lateinit var generator: MessageGenerator

    @Before
    fun setUp() {
        engineMock = mockk<Engine>(relaxed = true)
        generator = MessageGenerator(engineMock)
    }


    /****/

    @Test
    fun testInitialiseEngineCallsInitializeFunctionExactlyOnce() {
        generator.initialiseEngine()
        generator.initialiseEngine()
        generator.initialiseEngine()
        verify(exactly = 1) { engineMock.initialize() }
    }

    @Test
    fun testInitialiseEngineDoesNotThrowException() {
        every { engineMock.initialize() } throws IllegalStateException("already initialised")

        generator.initialiseEngine()
    }


    /****/

    @Test
    fun testGenerateResponseCreatesAndClosesConversation() = runTest {
        val conversationMock = mockk<Conversation>()
        every { engineMock.createConversation(any()) } returns conversationMock
        every { conversationMock.sendMessageAsync(any()) } returns flowOf()
        every { conversationMock.close() } just runs

        generator.generateResponse("test").collect()

        verify { engineMock.createConversation(any()) }
        verify { conversationMock.close() }
    }
}