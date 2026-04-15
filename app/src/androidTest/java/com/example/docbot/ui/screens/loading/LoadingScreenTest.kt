package com.example.docbot.ui.screens.loading

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LoadingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeViewModel: FakeLoadingViewModel = FakeLoadingViewModel()

    private fun setLoadingScreen(
        viewModel: LoadingViewModel = fakeViewModel,
        onReady: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            LoadingScreen(viewModel, onReady)
        }
    }


    /****/

    @Test
    fun testLoadingIndicatorIsDisplayed() {
        setLoadingScreen()
        composeTestRule.onNodeWithText("Loading model").assertExists()
    }

    @Test
    fun testInitialiseIsTriggeredOnLaunch() {
        setLoadingScreen()
        composeTestRule.waitForIdle()
        assertTrue(fakeViewModel.initialiseCalled)
    }

    @Test
    fun testOnReadyIsTriggeredOnLaunch() {
        var called = false
        setLoadingScreen(onReady = { called = true })
        composeTestRule.waitForIdle()
        assertTrue(called)
    }
}