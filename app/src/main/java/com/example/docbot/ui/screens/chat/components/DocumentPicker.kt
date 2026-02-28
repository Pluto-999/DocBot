package com.example.docbot.ui.screens.chat.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@Composable
fun DocumentPicker(
    onDismissRequest: () -> Unit,
    openDocumentPicker: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {

        Button(
            onClick = openDocumentPicker
        ) {
            Text("Choose Documents")
        }
    }
}