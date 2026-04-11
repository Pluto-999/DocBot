package com.example.docbot.ui.screens.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setSearchBar(
        value: String = "",
        onValueChange: (String) -> Unit = {}
    ) {
        composeTestRule.setContent {
            SearchBar(value, onValueChange)
        }
    }


    /****/

    @Test
    fun testSearchBarContentsAreDisplayed() {
        setSearchBar()

        composeTestRule.onNodeWithText("Search").assertExists()
        composeTestRule.onNodeWithContentDescription("Search").assertExists()

    }

    @Test
    fun testPlaceholderIsDisplayedWhenSearchBarIsClickedAndEmpty() {
        setSearchBar()

        composeTestRule.onNodeWithText("Search").performClick()
        composeTestRule.onNodeWithText("Enter a conversation title").assertExists()
    }

    @Test
    fun testValueIsDisplayedAndPlaceholderIsNotDisplayedWhenValueIsNotEmpty() {
        setSearchBar(value = "test")

        composeTestRule.onNodeWithText("Enter a conversation title").assertDoesNotExist()
        composeTestRule.onNodeWithText("test").assertExists()
    }

    @Test
    fun testInputtingValueTriggersOnValueChangeCallback() {
        var inputValue = ""
        setSearchBar(onValueChange = { inputValue = it })

        composeTestRule.onNodeWithText("Search").performTextInput("test")

        assertEquals("test", inputValue)
    }

}