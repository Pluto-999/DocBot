package com.example.docbot.data.sources

import com.example.docbot.data.models.Conversation
import com.example.docbot.data.models.Conversation_
import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.models.Message_
import io.objectbox.Box
import io.objectbox.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageLocalDataSource @Inject constructor(
    private val messageBox: Box<Message>,
    private val conversationBox: Box<Conversation>
) {
    fun getMessages(conversationId: Long): Flow<List<Message>> {
        val builder = messageBox.query()

        builder.link(Message_.conversation)
            .apply(Conversation_.id.equal(conversationId))

        return builder.build().subscribe().toFlow()
    }

    fun insertMessage(
        conversationId: Long,
        messageContents: String,
        messageType: MessageType
    ) {
        val newMessage = Message(contents = messageContents, messageType = messageType)
        val conversation = conversationBox.get(conversationId)
        newMessage.conversation.setTarget(conversation)
        messageBox.put(newMessage)
    }
}