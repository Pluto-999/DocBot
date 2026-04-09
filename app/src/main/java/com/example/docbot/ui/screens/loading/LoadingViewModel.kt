package com.example.docbot.ui.screens.loading

import androidx.lifecycle.ViewModel
import com.example.docbot.data.message.generation.MessageGenerator
import com.example.docbot.ui.screens.loading.initialiser.ModelInitialiser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val messageGenerator: MessageGenerator,
    private val modelInitialiser: ModelInitialiser
): ViewModel() {

    suspend fun initialise(filesDir: File) = withContext(Dispatchers.IO) {
        modelInitialiser.initialiseModel(filesDir)
        messageGenerator.initialiseEngine()
    }
}