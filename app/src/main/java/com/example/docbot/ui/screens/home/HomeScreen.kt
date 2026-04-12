package com.example.docbot.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docbot.ui.screens.home.components.AppBar
import com.example.docbot.ui.screens.home.components.ConversationList
import com.example.docbot.ui.screens.home.components.NewConversationButton
import com.example.docbot.ui.screens.home.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel<HomeViewModelImpl>(),
    onConversationNavigate: (id: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                sortIconOnClick = { viewModel.toggleSortMenu() },
                sortMenuExpanded = uiState.sortMenuExpanded,
                sortMenuDismiss = { viewModel.collapseSortMenu() },
                filterIconOnClick = { viewModel.toggleFilterMenu() },
                filterMenuExpanded = uiState.filterMenuExpanded,
                filterMenuDismiss = { viewModel.collapseFilterMenu() },
                titleOnClick = { viewModel.updateTitleOrder() },
                titleIcon = {
                    Icon(
                        imageVector =
                            if (uiState.conversationOrder == ConversationOrder.TITLE_ASC) {
                                Icons.Default.ArrowUpward
                            } else {
                                Icons.Default.ArrowDownward
                            },
                        contentDescription =
                            if (uiState.conversationOrder == ConversationOrder.TITLE_ASC) {
                                "Title Ascending"
                            } else {
                                "Title Descending"
                            }
                    )
                },
                dateOnClick = { viewModel.updateDateOrder() },
                dateIcon = {
                    Icon(
                        imageVector =
                            if (uiState.conversationOrder == ConversationOrder.DATE_ASC) {
                                Icons.Default.ArrowUpward
                            }
                            else {
                                Icons.Default.ArrowDownward
                            },
                        contentDescription =
                            if (uiState.conversationOrder == ConversationOrder.DATE_ASC) {
                                "Date Ascending"
                            }
                            else {
                                "Date Descending"
                            }
                    )
                },
                favouriteOnClick = { viewModel.filterConversations(ConversationFilter.FAVOURITES) },
                deleteSoonOnClick = { viewModel.filterConversations(ConversationFilter.DELETE_SOON) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
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
                    viewModel.updateSearchQuery(newQuery = it)
                }
            )
            ConversationList(
                conversations = uiState.conversations,
                onConversationFavouriteClick = { conversationId, isFavourite ->
                    viewModel.toggleFavourite(conversationId, isFavourite)
               },
                onConversationDeleteClick = { viewModel.deleteConversation(it) },
                onConversationClick = { onConversationNavigate(it) },
                modifier = Modifier.weight(1f)
            )
            NewConversationButton(
                createConversation = {
                    val conversationId = viewModel.createConversation()
                    onConversationNavigate(conversationId)
                }
            )
        }
    }
}