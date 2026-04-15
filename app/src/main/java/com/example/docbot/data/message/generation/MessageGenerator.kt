package com.example.docbot.data.message.generation

import android.util.Log
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class MessageGenerator @Inject constructor(
    private val engine: Engine
) {

    private var engineInitialised = false

    fun initialiseEngine() {
        if (!engineInitialised) {
            try {
                engine.initialize()
                engineInitialised = true
            }
            catch (e: IllegalStateException) {
                Log.e("Engine initialisation", "$e")
            }
        }
    }

    fun generateResponse(fullPrompt: String): Flow<com.google.ai.edge.litertlm.Message> {

        val config = ConversationConfig(
            samplerConfig = SamplerConfig(topK = 20, topP = 0.9, temperature = 0.3)
        )

        val conversation = engine.createConversation(config)

        val messageFlow = conversation.sendMessageAsync(
            com.google.ai.edge.litertlm.Message.of(fullPrompt)
        )

        return messageFlow
            .onCompletion {
                conversation.close()
            }
    }
}