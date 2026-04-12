package com.example.docbot.ui.screens.chat.components

import androidx.compose.runtime.Composable

@Composable
fun ConversationTitleDialog(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    value: String,
    onValueChange: (title: String) -> Unit
) {
    if (isOpen) {
        UpdateConversationTitle(
            onDismissRequest = { onDismissRequest() },
            value = value,
            onValueChange = {
                onValueChange(it)
            }
        )
    }
}