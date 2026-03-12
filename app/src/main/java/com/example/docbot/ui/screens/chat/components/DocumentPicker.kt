package com.example.docbot.ui.screens.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.docbot.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPicker(
    onDismissRequest: () -> Unit,
    openDocumentPicker: () -> Unit,
    documentNames: List<String>
) {

    ModalBottomSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth()
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