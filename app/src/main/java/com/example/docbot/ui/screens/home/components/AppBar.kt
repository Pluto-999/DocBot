package com.example.docbot.ui.screens.home.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.docbot.ui.screens.home.HomeViewModel
import com.example.docbot.ui.screens.home.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    sortIconOnClick: () -> Unit,
    sortMenuExpanded: Boolean,
    sortMenuDismiss: () -> Unit,
    filterIconOnClick: () -> Unit,
    filterMenuExpanded: Boolean,
    filterMenuDismiss: () -> Unit,
    titleOnClick: () -> Unit,
    titleIcon: @Composable () -> Unit,
    dateOnClick: () -> Unit,
    dateIcon: @Composable () -> Unit,

    ) {
    TopAppBar(
        title = {
            Text(
                text = "Conversations"
            )
        },
        actions = {
            Box {
                IconButton(
                    onClick = sortIconOnClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(28.dp)
                    )
                }
                MenuDropdown(
                    expanded = sortMenuExpanded,
                    onDismiss = sortMenuDismiss,
                    menuContents = {
                        DropdownMenuItem(
                            text = { Text("Title") },
                            leadingIcon = { titleIcon() },
                            onClick = titleOnClick
                        )
                        DropdownMenuItem(
                            text = { Text("Date") },
                            leadingIcon = { dateIcon() },
                            onClick = dateOnClick
                        )
                    }
                )
            }

            Box {
                IconButton(
                    onClick = filterIconOnClick
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filter",
                        modifier = Modifier.size(28.dp)
                    )
                }
                MenuDropdown(
                    expanded = filterMenuExpanded,
                    onDismiss = filterMenuDismiss,
                    menuContents = {
                        DropdownMenuItem(
                            text = { Text("Favourites") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favourites"
                                )
                            },
                            onClick = {/* Do something */}
                        )
                        DropdownMenuItem(
                            text = { Text("Soon to be deleted") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Soon to be deleted"
                                )
                            },
                            onClick = {/* Do something */}
                        )
                    }
                )
            }
        },
        modifier = Modifier
    )
}