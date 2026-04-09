package com.example.docbot.ui.screens.loading

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.docbot.data.message.generation.MessageGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val messageGenerator: MessageGenerator
): ViewModel() {

    suspend fun initialise(context: Context) = withContext(Dispatchers.IO) {
        initialiseModelFile(context)
        messageGenerator.initialiseEngine()
    }

    private fun initialiseModelFile(context: Context) {
        val modelDirectory = File(context.filesDir, "slm")
        modelDirectory.mkdirs()

        val fileName = "gemma-3n-E2B-it-int4.litertlm"
        val source = File("/data/local/tmp/slm/$fileName")
        val destination = File(modelDirectory, fileName)
        if (!destination.exists() || destination.length() != source.length()) {
            File("/data/local/tmp/slm/$fileName")
                .copyTo(target = destination, overwrite = true)
        }
    }
}