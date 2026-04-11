package com.example.docbot.ui.screens.home.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MenuDropdownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setMenuDropdown(
        expanded: Boolean = true,
        onDismiss: () -> Unit = {},
        menuContents: @Composable () -> Unit = { Text("Contents") }
    ) {
        composeTestRule.setContent {
            MenuDropdown(expanded, onDismiss, menuContents)
        }
    }


    /****/

    @Test
    fun testMenuContentsAreDisplayedWhenMenuIsExpanded() {
        setMenuDropdown()
        composeTestRule.onNodeWithText("Contents").assertExists()
    }

    @Test
    fun testMenuContentsNotDisplayedWhenMenuIsNotExpanded() {
        setMenuDropdown(expanded = false)
        composeTestRule.onNodeWithText("Contents").assertDoesNotExist()
    }

    @Test
    fun testBackPressTriggersOnDismissCallback() {
        var dismissed = false
        setMenuDropdown(onDismiss = { dismissed = true })

        Espresso.pressBack()

        assertTrue(dismissed)
    }
}