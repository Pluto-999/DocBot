package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DocumentPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val documentNames = listOf("document 1", "document 2", "document 3")

    private fun setDocumentPicker(
        onDismissRequest: () -> Unit = {},
        openDocumentPicker: () -> Unit = {},
        documentNames: List<String> = this.documentNames
    ) {
        composeTestRule.setContent {
            DocumentPicker(onDismissRequest, openDocumentPicker, documentNames)
        }
    }


    /****/

    @Test
    fun testNoDocumentNamesAreDisplayedWhenListIsEmpty() {
        setDocumentPicker(documentNames = emptyList())

        composeTestRule.onNodeWithText("No Documents").assertExists()
        composeTestRule.onNodeWithText("Current Documents").assertDoesNotExist()
    }

    @Test
    fun testDocumentNamesAreDisplayed() {
        setDocumentPicker()

        composeTestRule.onNodeWithText("Current Documents").assertExists()
        composeTestRule.onNodeWithText("No Documents").assertDoesNotExist()

        for (name in documentNames) {
            composeTestRule.onNodeWithText(name).assertExists()
        }
    }

    @Test
    fun testBackPressTriggersOnDismissRequestCallback() {
        var clicked = false
        setDocumentPicker(onDismissRequest = { clicked = true })

        Espresso.pressBack()

        assertTrue(clicked)
    }

    @Test
    fun testChooseDocumentsButtonExists() {
        setDocumentPicker()
        composeTestRule.onNodeWithText("Choose Documents").assertExists()
    }

    @Test
    fun testChooseDocumentButtonTriggersOpenDocumentPickerCallbackWhenClicked() {
        var clicked = false
        setDocumentPicker(openDocumentPicker = { clicked = true })

        composeTestRule.onNodeWithText("Choose Documents").performClick()

        assertTrue(clicked)
    }
}