package com.example.docbot.ui.screens.loading.initialiser

import android.util.Log
import java.io.File
import javax.inject.Inject

class ModelInitialiser @Inject constructor(
    private val sourceDirectory: File
) {

    private var modelInitialised = false

    fun initialiseModel(filesDirectory: File) {
        if (!modelInitialised) {
            try {
                val modelDirectory = File(filesDirectory, "slm")
                modelDirectory.mkdirs()

                val fileName = "gemma-3n-E2B-it-int4.litertlm"

                val source = File(sourceDirectory, fileName)
                val destination = File(modelDirectory, fileName)

                if (!destination.exists() || destination.length() != source.length()) {
                    source.copyTo(target = destination, overwrite = true)
                }
                modelInitialised = true
            }
            catch (e: Exception) {
                Log.e("Model Initialisation", "$e")
            }

        }
    }
}