package com.example.docbot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.docbot.ui.screens.chat.ChatScreen
import com.example.docbot.ui.screens.home.HomeScreen

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(HomePage)

    NavDisplay(
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = backStack,
        entryProvider = entryProvider {
            entry<HomePage> {
                HomeScreen(
                    onConversationNavigate = {
                        backStack.add(ChatPage(id = it))
                    }
                )
            }
            entry<ChatPage> {
                key -> ChatScreen(key.id)
            }
        }
    )
}