package com.example.docbot.data.repositories

import com.example.docbot.data.models.Message
import com.example.docbot.data.models.MessageType
import com.example.docbot.data.sources.MessageLocalDataSource
import com.google.ai.edge.litertlm.Engine
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource,
    private val engine: Engine
) : MessageRepository {

    var engineInitialised = false

    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageLocalDataSource.getMessages(conversationId)
    }

    override suspend fun sendMessage(conversationId: Long, message: String):
            Flow<com.google.ai.edge.litertlm.Message>
    {
        // add user's message
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.PROMPT
        )

        // generate response from the model
        val initialisedEngine = getInitialisedEngine()
        val conversation = initialisedEngine.createConversation()
        val messageFlow = conversation.sendMessageAsync(com.google.ai.edge.litertlm.Message.of(message))

        return messageFlow
    }

    override fun saveResponse(conversationId: Long, message: String) {
        messageLocalDataSource.insertMessage(
            conversationId,
            message,
            MessageType.RESPONSE
        )
    }

    private suspend fun getInitialisedEngine(): Engine {
        if (!engineInitialised) {
            engine.initialize()
            engineInitialised = true
        }
        return engine
    }
}