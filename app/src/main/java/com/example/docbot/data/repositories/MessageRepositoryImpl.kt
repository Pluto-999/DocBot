package com.example.docbot.data.repositories

import android.util.Log
import com.example.docbot.data.models.Message
import com.example.docbot.data.sources.MessageLocalDataSource
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageLocalDataSource: MessageLocalDataSource
) : MessageRepository {
    override fun getMessages(conversationId: Long): Flow<List<Message>> {
        return messageLocalDataSource.getMessages(conversationId)
    }

    override fun sendMessage(conversationId: Long, message: String) {
        // add user's message
        messageLocalDataSource.addMessage(conversationId, message)

        // generate model's response from this message
        val engineConfig = EngineConfig(
            modelPath = "/data/local/tmp/slm/gemma-3n-E2B-it-int4.litertlm",
            backend = Backend.CPU
        )
        val engine = Engine(engineConfig)

        CoroutineScope(Dispatchers.IO).launch {
            engine.initialize()

            val conversation = engine.createConversation()

            conversation.sendMessageAsync(com.google.ai.edge.litertlm.Message.of(message))
//                    .catch { ... }
                .collect { value -> Log.e("SLM RESPONSE !!!", value.toString()) }


            conversation.close()
            engine.close()
        }

    }

}