package com.example.docbot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.docbot.ui.screens.chat.ChatScreen
import com.example.docbot.ui.screens.home.HomeScreen
import com.example.docbot.ui.screens.loading.LoadingScreen

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(LoadingPage)

    NavDisplay(
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = backStack,
        entryProvider = entryProvider {
            entry<LoadingPage> {
                LoadingScreen(
                    onReady = {
                        backStack.removeLastOrNull()
                        backStack.add(HomePage)
                    }
                )
            }
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