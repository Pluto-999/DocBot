package com.example.docbot.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docbot.ui.screens.home.components.AppBar
import com.example.docbot.ui.screens.home.components.ConversationCard
import com.example.docbot.ui.screens.home.components.NewConversationButton
import com.example.docbot.ui.screens.home.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel<HomeViewModel>(),
    onConversationNavigate: (id: Long) -> Unit
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
                    if (uiState.conversationOrder == ConversationOrder.TITLE_ASC) {
                        viewModel.updateConversationOrder(ConversationOrder.TITLE_DESC)
                    }
                    else {
                        viewModel.updateConversationOrder(ConversationOrder.TITLE_ASC)
                    }
                },
                titleIcon = {
                    Icon(
                        imageVector =
                            if (uiState.conversationOrder == ConversationOrder.TITLE_ASC) {
                                Icons.Default.ArrowUpward
                            } else {
                                Icons.Default.ArrowDownward
                            },
                        contentDescription = "Sort Title"
                    )
                },
                dateOnClick = {
                    if (uiState.conversationOrder == ConversationOrder.DATE_ASC) {
                        viewModel.updateConversationOrder(ConversationOrder.DATE_DESC)
                    }
                    else {
                        viewModel.updateConversationOrder(ConversationOrder.DATE_ASC)
                    }
                },
                dateIcon = {
                    Icon(
                        imageVector =
                            if (uiState.conversationOrder == ConversationOrder.DATE_ASC) {
                                Icons.Default.ArrowUpward
                            }
                            else {
                                Icons.Default.ArrowDownward
                            },
                        contentDescription = "Sort Date"
                    )
                },
                favouriteOnClick = {
                    if (uiState.conversationFilter != ConversationFilter.FAVOURITES) {
                        viewModel.updateConversationFilter(ConversationFilter.FAVOURITES)
                    }
                    else {
                        viewModel.updateConversationFilter(ConversationFilter.NONE)
                    }
                },
                deleteSoonOnClick = {
                    if (uiState.conversationFilter != ConversationFilter.DELETE_SOON) {
                        viewModel.updateConversationFilter(ConversationFilter.DELETE_SOON)
                    }
                    else {
                        viewModel.updateConversationFilter(ConversationFilter.NONE)
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = uiState.snackbarHostState)
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
                viewModel = viewModel,
                onConversationClick = onConversationNavigate,
                modifier = Modifier.weight(1f)
            )
            NewConversationButton(viewModel)
        }
    }
}


/*** List of Conversations ***/
@Composable
fun ConversationList(
    conversations: List<ConversationState>,
    viewModel: HomeViewModel,
    onConversationClick: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(conversations) { conversation ->
            ConversationCard(
                title = conversation.title,
                date = conversation.date,
                isFavourite = conversation.isFavourite,
                openConversation = { onConversationClick(conversation.id) },
                favouriteClick = { viewModel.toggleFavourite(conversation.id, conversation.isFavourite) },
                deleteClick = { viewModel.deleteConversation(conversation.id) }
            )
        }
    }
}
