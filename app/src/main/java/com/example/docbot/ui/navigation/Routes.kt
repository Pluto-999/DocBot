package com.example.docbot.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomePage: NavKey
@Serializable
data class ChatPage(val id: Long): NavKey