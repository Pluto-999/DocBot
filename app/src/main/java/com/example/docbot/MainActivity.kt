package com.example.docbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.docbot.data.ObjectBox
import com.example.docbot.ui.screens.home.HomeScreen
import com.example.docbot.ui.theme.DocBotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ObjectBox.init(this)
        enableEdgeToEdge()
        setContent {
            DocBotTheme {
                HomeScreen()
            }
        }
    }
}