package com.example.docbot.ui.screens.chat.components

import androidx.compose.runtime.Composable

@Composable
fun LoadingIndicators(
    isDocumentProcessing: Boolean,
    isModelInference: Boolean
) {

    if (isDocumentProcessing) {
        LoadingIndicator(message = "Processing document")
    }

    if (isModelInference) {
        LoadingIndicator(message = "Model generating response")
    }
}