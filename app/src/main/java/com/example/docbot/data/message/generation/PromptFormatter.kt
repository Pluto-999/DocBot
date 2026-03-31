package com.example.docbot.data.message.generation

import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import javax.inject.Inject
import kotlin.collections.forEach

class PromptFormatter @Inject constructor() {

    fun formatMessageContext(context: List<String>): String {
        if (context.isEmpty()) return ""

        return """
            Use the following context, if relevant, when answering this prompt, alongside your own knowledge if required:
            ${context.joinToString("\n")}
        """.trimIndent()
    }

    fun formatPreviousMessages(
        previousMessages: List<Message>
    ): String {
        val previousMessagesFormatted = mutableListOf<String>()

        previousMessages.forEach { message ->
            if (message.messageType == MessageType.PROMPT) {
                previousMessagesFormatted.add("Prompt asked by the user: ${message.contents}")
            }
            else if (message.messageType == MessageType.RESPONSE) {
                previousMessagesFormatted.add("The response given by you: ${message.contents}")
            }
        }

        val previousMessagesString =
            if (previousMessages.isEmpty()) {
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