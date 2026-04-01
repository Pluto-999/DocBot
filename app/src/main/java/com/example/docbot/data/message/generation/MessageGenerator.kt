package com.example.docbot.data.message.generation

import android.os.Debug
import android.util.Log
import com.google.ai.edge.litertlm.Engine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class MessageGenerator @Inject constructor(
    private val engine: Engine
) {

    private var engineInitialised = false

    fun initialiseEngine() {
        if (!engineInitialised) {
            engine.initialize()
            engineInitialised = true
        }
    }

    fun generateResponse(fullPrompt: String): Flow<com.google.ai.edge.litertlm.Message> {

        val conversation = engine.createConversation()

        val messageFlow = conversation.sendMessageAsync(
            com.google.ai.edge.litertlm.Message.of(fullPrompt)
        )

        return messageFlow
            .onCompletion {
                // TESTING MEMORY USAGE OF INFERENCE !!
                val mi = Debug.MemoryInfo()
                Debug.getMemoryInfo(mi)
                Log.e("MEMORY", "Native: ${mi.nativePss} KB")

                conversation.close()
            }
    }
}