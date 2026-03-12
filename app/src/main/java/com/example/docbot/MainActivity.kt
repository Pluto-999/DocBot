package com.example.docbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.docbot.data.repositories.ConversationRepository
import com.example.docbot.ui.navigation.Navigation
import com.example.docbot.ui.theme.DocBotTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var conversationRepository: ConversationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        conversationRepository.scheduleOldConversationsDeletion()
        enableEdgeToEdge()
        setContent {
            DocBotTheme {
                Navigation()
            }
        }
    }
}