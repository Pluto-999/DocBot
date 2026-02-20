package com.example.docbot.ui.screens.chat.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun UpdateConversationTitle(
    onDismissRequest: () -> Unit,
    value: String,
    onValueChange: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text("Change conversation name")
                },
                placeholder = {
                    Text("New name")
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDismissRequest() }
                ),
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 16.dp,
                    start = 12.dp,
                    end = 12.dp
                )
            )
        }
    }
}