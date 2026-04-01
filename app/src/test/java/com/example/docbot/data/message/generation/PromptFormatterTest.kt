package com.example.docbot.data.message.generation

import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import org.junit.Assert.*
import org.junit.Test

class PromptFormatterTest {
    private val promptFormatter = PromptFormatter()

    @Test
    fun testFormatMessageContextReturnsEmptyStringWithEmptyContext() {
        assertEquals("", promptFormatter.formatMessageContext(emptyList()))
    }

    @Test
    fun testFormatMessageContextIncludesAllChunksAndContextualMessage() {
        val result = promptFormatter.formatMessageContext(listOf("chunk1", "chunk2"))

        assertTrue(result.contains("chunk1"))
        assertTrue(result.contains("chunk2"))
        assertTrue(result.contains("Use the following context"))
    }


    /****/

    @Test
    fun testFormatPreviousMessagesReturnsEmptyStringWithEmptyPreviousMessages() {
        assertEquals("", promptFormatter.formatPreviousMessages(emptyList()))
    }

    @Test
    fun testFormatPreviousMessagesIncludesPromptsAndResponsesAndContextualMessage() {
        val messages = listOf(
            Message(contents = "message 1", messageType = MessageType.PROMPT),
            Message(contents = "message 2", messageType = MessageType.RESPONSE)
        )

        val result = promptFormatter.formatPreviousMessages(messages)

        assertTrue(result.contains("Prompt asked by the user: message 1"))
        assertTrue(result.contains("The response given by you: message 2"))
        assertTrue(result.contains("Previous prompts and responses"))
    }

    @Test
    fun testFormatPreviousMessagesDoesNotIncludeAnyResponsesIfNoneExist() {
        val messages = listOf(
            Message(contents = "message 1", messageType = MessageType.PROMPT),
            Message(contents = "message 2", messageType = MessageType.PROMPT)
        )

        val result = promptFormatter.formatPreviousMessages(messages)

        assertTrue(result.contains("Prompt asked by the user: message 1"))
        assertTrue(result.contains("Prompt asked by the user: message 2"))
        assertTrue(result.contains("Previous prompts and responses"))
        assertFalse(result.contains("The response given by you:"))
    }

    @Test
    fun testFormatPreviousMessagesReturnsEmptyStringIfAllMessagesAreOfTypeUnknown() {
        val messages = listOf(
            Message(contents = "message 1", messageType = MessageType.UNKNOWN),
            Message(contents = "message 2", messageType = MessageType.UNKNOWN)
        )

        val result = promptFormatter.formatPreviousMessages(messages)

        assertEquals("", result)
    }
}