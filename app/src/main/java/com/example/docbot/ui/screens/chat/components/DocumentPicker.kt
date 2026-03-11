package com.example.docbot.ui.screens.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.docbot.ui.theme.Typography

@Composable
fun DocumentPicker(
    onDismissRequest: () -> Unit,
    openDocumentPicker: () -> Unit,
    documentNames: List<String>
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                if (documentNames.isEmpty()) {
                    Text(
                        text = "No Documents",
                        style = Typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                else {
                    Text(
                        text = "Current Documents",
                        style = Typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                documentNames.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                Button(
                    onClick = openDocumentPicker,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Choose Documents")
                }
            }
        }
    }
}