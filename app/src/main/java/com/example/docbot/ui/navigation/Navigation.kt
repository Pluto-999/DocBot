package com.example.docbot.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.docbot.ui.screens.chat.ChatScreen
import com.example.docbot.ui.screens.home.HomeScreen

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(HomePage)

    NavDisplay(
        backStack = backStack,
        entryProvider = { key ->
            when (key) {
                is HomePage -> {
                    NavEntry(key) {
                        HomeScreen(
                            onConversationNavigate = {
                                backStack.add(ChatPage(id = it))
                            }
                        )
                    }
                }
                is ChatPage -> {
                    NavEntry(key) {
                        ChatScreen(key.id)
                    }
                }
                else -> {
                    NavEntry(key) {
                        Text("Unknown route")
                    }
                }
            }
        }
    )
}