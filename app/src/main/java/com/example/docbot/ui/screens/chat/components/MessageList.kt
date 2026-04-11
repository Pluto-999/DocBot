package com.example.docbot.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.docbot.data.models.MessageType
import com.example.docbot.ui.screens.chat.MessageState

@Composable
fun MessageList(
    messages: List<MessageState>,
    currentResponse: String,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        state = listState,
        modifier = modifier
            .fillMaxWidth()
    ) {
        items(messages) { message ->
            Row(
                horizontalArrangement =
                    if (message.messageType == MessageType.PROMPT) Arrangement.End
                    else Arrangement.Start,
                modifier = modifier.fillMaxWidth()
            ) {
                Box (
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (message.messageType == MessageType.PROMPT) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                        .padding(12.dp)
                ){
                    Text(message.contents)
                }
            }
        }
        if (currentResponse.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Box (
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp)
                    ){
                        Text(currentResponse)
                    }
                }
            }
        }
    }
}