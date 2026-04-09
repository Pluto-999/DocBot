package com.example.docbot.ui.screens.loading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.docbot.ui.screens.chat.components.LoadingIndicator


@Composable
fun LoadingScreen(
    viewModel: LoadingViewModel = hiltViewModel<LoadingViewModel>(),
    onReady: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialise(context.filesDir)
        onReady()
    }

    LoadingIndicator("Loading model")
}