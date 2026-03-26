package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.models.Message_
import io.objectbox.Box
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

const val PREVIOUS_MESSAGES_LIMIT: Long = 3

class MessageLocalDataSource @Inject constructor(
    private val messageBox: Box<Message>,
    private val conversationBox: Box<Conversation>
) {
    fun insertMessage(
        conversationId: Long,
        messageContents: String,
        messageType: MessageType
    ) {

        val newMessage = Message(contents = messageContents, messageType = messageType)
        val dateTime = newMessage.timestamp

        val conversation = conversationBox.get(conversationId)

        conversation.latestMessage = dateTime

        newMessage.conversation.setTarget(conversation)

        messageBox.put(newMessage)
        conversationBox.put(conversation)
    }

    fun getMessages(conversationId: Long): Flow<List<Message>> {
        val builder = messageBox.query()

        builder.link(Message_.conversation)
            .apply(Conversation_.id.equal(conversationId))

        builder.order(Message_.timestamp)

        return builder.build().subscribe().toFlow()
    }

    fun getRecentMessages(conversationId: Long): List<Message> {
        val promptBuilder = messageBox.query(Message_.messageType.equal(0)) // 0 = PROMPT
        promptBuilder.link(Message_.conversation)
            .apply(Conversation_.id.equal(conversationId))

        promptBuilder.order(Message_.timestamp, QueryBuilder.DESCENDING)

        val responseBuilder = messageBox.query(Message_.messageType.equal(1)) // 1 = RESPONSE
        responseBuilder.link(Message_.conversation)
            .apply(Conversation_.id.equal(conversationId))

        responseBuilder.order(Message_.timestamp, QueryBuilder.DESCENDING)

        val prompts = promptBuilder.build().find(1, PREVIOUS_MESSAGES_LIMIT).reversed()
        val responses = responseBuilder.build().find(0, PREVIOUS_MESSAGES_LIMIT).reversed()

        return prompts.zip(responses) { a, b -> listOf(a, b) }.flatten()
    }
}