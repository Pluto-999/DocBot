package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LoadingIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setLoadingIndicator(message: String = "loading") {
        composeTestRule.setContent {
            LoadingIndicator(message)
        }
    }


    /****/

    @Test
    fun testLoadingMessageIsDisplayed() {
        setLoadingIndicator()
        composeTestRule.onNodeWithText("loading").assertExists()
    }

    @Test
    fun testCircularProgressIndicatorIsDisplayed() {
        setLoadingIndicator()
        composeTestRule.onNodeWithContentDescription("Loading indicator").assertExists()
    }
}