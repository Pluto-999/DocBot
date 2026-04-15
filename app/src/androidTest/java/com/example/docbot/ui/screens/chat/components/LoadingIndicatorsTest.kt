package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class LoadingIndicatorsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setLoadingIndicators(
        isDocumentProcessing: Boolean = false,
        isModelInference: Boolean = false
    ) {
        composeTestRule.setContent {
            LoadingIndicators(isDocumentProcessing, isModelInference)
        }
    }


    /****/

    @Test
    fun testDocumentProcessingIndicatorIsNotDisplayedWhenBooleanIsFalse() {
        setLoadingIndicators()
        composeTestRule.onNodeWithText("Processing document").assertDoesNotExist()
    }

    @Test
    fun testDocumentProcessingIndicatorIsDisplayedWhenBooleanIsTrue() {
        setLoadingIndicators(isDocumentProcessing = true)
        composeTestRule.onNodeWithText("Processing document").assertExists()
    }

    @Test
    fun testModelInferenceIndicatorIsNotDisplayedWhenBooleanIsFalse() {
        setLoadingIndicators()
        composeTestRule.onNodeWithText("Model generating response").assertDoesNotExist()
    }

    @Test
    fun testModelInferenceIndicatorIsDisplayedWhenBooleanIsTrue() {
        setLoadingIndicators(isModelInference = true)
        composeTestRule.onNodeWithText("Model generating response").assertExists()
    }

}