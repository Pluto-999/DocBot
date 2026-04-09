package com.example.docbot.ui.screens.loading.initialiser

import java.io.File
import javax.inject.Inject

class ModelInitialiser @Inject constructor() {
    fun initialiseModel(filesDir: File) {
        val modelDirectory = File(filesDir, "slm")
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