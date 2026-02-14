package com.example.docbot.ui.screens.home.components


import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable

@Composable
fun MenuDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    menuContents: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() }
    ) {
        menuContents()
    }
}
