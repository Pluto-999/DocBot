package com.example.docbot.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docbot.ui.components.ConversationCard
import com.example.docbot.ui.components.MenuDropdown
import com.example.docbot.ui.components.SearchBar

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel<HomeViewModel>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterExpanded = uiState.filterMenuExpanded
    val sortExpanded = uiState.sortMenuExpanded

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Conversations"
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { viewModel.toggleSortMenu(!sortExpanded) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        MenuDropdown(
                            expanded = sortExpanded,
                            onDismiss = { viewModel.toggleSortMenu(false) },
                            menuContents = {
                                DropdownMenuItem(
                                    text = { Text("Title") },
                                    leadingIcon = {
                                        if (uiState.titleSort == SortType.ASC) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Sort Title Ascending"
                                            )
                                        }
                                        else {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Sort Title Descending"
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (uiState.titleSort == SortType.ASC) {
                                            viewModel.updateTitleSort(SortType.DESC)
                                        }
                                        else {
                                            viewModel.updateTitleSort(SortType.ASC)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Date") },
                                    leadingIcon = {
                                        if (uiState.dateSort == SortType.ASC) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Sort Date Ascending"
                                            )
                                        }
                                        else {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Sort Date Descending"
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (uiState.dateSort == SortType.ASC) {
                                            viewModel.updateDateSort(SortType.DESC)
                                        }
                                        else {
                                            viewModel.updateDateSort(SortType.ASC)
                                        }
                                    }
                                )
                            }
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { viewModel.toggleFilterMenu(!filterExpanded) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        MenuDropdown(
                            expanded = filterExpanded,
                            onDismiss = { viewModel.toggleFilterMenu(false) },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = ({/* Create Conversation */ }),
                icon = {
                    Icon(
                        Icons.Default.Add,
                        "Create Conversation")
                },
                text = {
                    Text("New Conversation")
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Box {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                SearchBar(
                    value = uiState.searchQuery,
                    onValueChange = {
                        viewModel.updateSearchQuery(
                            newQuery = it
                        )
                    }
                )
                ConversationList()
            }
        }
    }
}


/*** List of Conversations ***/
@Composable
fun ConversationList() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(100) { index ->
            ConversationCard(index)
        }
    }
}


