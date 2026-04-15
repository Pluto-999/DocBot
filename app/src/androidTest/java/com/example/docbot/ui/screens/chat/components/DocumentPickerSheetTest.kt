package com.example.docbot.ui.screens.chat.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class DocumentPickerSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setDocumentPickerSheet(
        isOpen: Boolean = true,
        onDismissRequest: () -> Unit = {},
        openDocumentPicker: () -> Unit = {},
        documentNames: List<String> = emptyList()
    ) {
        composeTestRule.setContent {
            DocumentPickerSheet(isOpen, onDismissRequest, openDocumentPicker, documentNames)
        }
    }


    /****/

    @Test
    fun testDocumentPickerIsNotDisplayedWhenIsOpenIsFalse() {
        setDocumentPickerSheet(isOpen = false)
        composeTestRule.onNodeWithText("Choose Documents").assertDoesNotExist()
    }

    @Test
    fun testDocumentPickerIsDisplayedWhenIsOpenIsTrue() {
        setDocumentPickerSheet(isOpen = true)
        composeTestRule.onNodeWithText("Choose Documents").assertExists()
    }
}