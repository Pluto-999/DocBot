package com.example.docbot.ui.screens.chat.components

import androidx.compose.runtime.Composable

@Composable
fun DocumentPickerSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    openDocumentPicker: () -> Unit,
    documentNames: List<String>
) {
    if (isOpen) {
        DocumentPicker(
            onDismissRequest = { onDismissRequest() },
            openDocumentPicker = { openDocumentPicker() },
            documentNames = documentNames
        )
    }
}