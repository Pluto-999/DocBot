package com.example.docbot.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.docbot.ui.screens.home.HomeViewModel

@Composable
fun NewConversationButton(
    viewModel: HomeViewModel
) {
    Button(
        onClick = { viewModel.createConversation() },
        modifier = Modifier
            .padding(bottom = 4.dp, top = 8.dp)
            .fillMaxWidth(0.9f)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                Icons.Default.Add,
                "Create Conversation"
            )
            Text("New Conversation")
        }
    }
}