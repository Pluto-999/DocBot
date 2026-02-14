package com.example.docbot.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.docbot.ui.screens.home.components.AppBar
import com.example.docbot.ui.screens.home.components.ConversationCard
import com.example.docbot.ui.screens.home.components.MenuDropdown
import com.example.docbot.ui.screens.home.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel<HomeViewModel>()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterExpanded = uiState.filterMenuExpanded
    val sortExpanded = uiState.sortMenuExpanded

    Scaffold(
        topBar = {
            AppBar(
                sortIconOnClick = { viewModel.toggleSortMenu(!sortExpanded) },
                sortMenuExpanded = sortExpanded,
                sortMenuDismiss = { viewModel.toggleSortMenu(false) },
                filterIconOnClick = { viewModel.toggleFilterMenu(!filterExpanded) },
                filterMenuExpanded = filterExpanded,
                filterMenuDismiss = { viewModel.toggleFilterMenu(false) },
                titleOnClick = {
                    if (uiState.titleSort == SortType.ASC) {
                        viewModel.updateTitleSort(SortType.DESC)
                    } else {
                        viewModel.updateTitleSort(SortType.ASC)
                    }
                },
                titleIcon = {
                    Icon(
                        imageVector =
                            if (uiState.titleSort == SortType.ASC) {
                                Icons.Default.ArrowUpward
                            } else {
                                Icons.Default.ArrowDownward
                            },
                        contentDescription = "Sort Title"
                    )
                },
                dateOnClick = {
                    if (uiState.dateSort == SortType.ASC) {
                        viewModel.updateDateSort(SortType.DESC)
                    }
                    else {
                        viewModel.updateDateSort(SortType.ASC)
                    }
                },
                dateIcon = {
                    Icon(
                        imageVector =
                            if (uiState.dateSort == SortType.ASC) {
                                Icons.Default.ArrowUpward
                            }
                            else {
                                Icons.Default.ArrowDownward
                            },
                        contentDescription = "Sort Date"
                    )
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.createConversation() },
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
                ConversationList(
                    conversations = uiState.conversations,
                    viewModel = viewModel
                )
            }
        }
    }
}


/*** List of Conversations ***/
@Composable
fun ConversationList(
    conversations: List<ConversationState>,
    viewModel: HomeViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(conversations) { conversation ->
            ConversationCard(
                title = conversation.title,
                date = conversation.date,
                isFavourite = conversation.isFavourite,
                deleteClick = { viewModel.deleteConversation(conversation.id) }
            )
        }
    }
}
