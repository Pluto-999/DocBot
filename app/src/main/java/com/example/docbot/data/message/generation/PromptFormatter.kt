package com.example.docbot.data.message.generation

import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import javax.inject.Inject
import kotlin.collections.forEach

class PromptFormatter @Inject constructor() {

    fun formatMessageContext(context: List<String>): String {
        if (context.isEmpty()) return ""

        return """
            Use the following context, if relevant, alongside your own knowledge if required:
            ${context.joinToString("\n")}
        """.trimIndent()
    }

    fun formatPreviousMessages(
        previousMessages: List<Message>
    ): String {
        val previousMessagesFormatted = mutableListOf<String>()

        previousMessages.forEach { message ->
            when (message.messageType) {
                MessageType.PROMPT -> previousMessagesFormatted.add("Prompt asked by the user: ${message.contents}")
                MessageType.RESPONSE -> previousMessagesFormatted.add("The response given by you: ${message.contents}")
                else -> ""
            }
        }

        val previousMessagesString =
            if (previousMessagesFormatted.isEmpty()) {
                ""
            }
            else {
                """
                    Previous prompts and responses in chronological order, if relevant:
                    ${previousMessagesFormatted.joinToString("\n")}
                """.trimIndent()
            }

        return previousMessagesString
    }
}