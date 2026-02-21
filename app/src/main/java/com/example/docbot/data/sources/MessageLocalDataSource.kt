package com.example.docbot.data.sources

import com.example.docbot.data.models.Message
import com.example.docbot.data.models.Message_
import io.objectbox.Box
import io.objectbox.kotlin.equal
import io.objectbox.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageLocalDataSource @Inject constructor(
    private val messageBox: Box<Message>
) {
    fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageBox
            .query(Message_.conversationId equal conversationId)
            .build()
            .subscribe()
            .toFlow()
    }

    fun addMessage(conversationId: Long, newMessage: String) {
        messageBox.put(Message(
            contents = newMessage,
            messageType = "PROMPT",
            conversationId = conversationId
        ))
    }
}