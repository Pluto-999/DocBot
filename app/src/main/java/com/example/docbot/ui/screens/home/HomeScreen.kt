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
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docbot.ui.components.ConversationCard
import com.example.docbot.ui.components.MenuDropdown
import com.example.docbot.ui.components.SearchBar

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel<HomeViewModel>()
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery = uiState.searchQuery

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Conversations"
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                /* Sort */
                                sortExpanded = !sortExpanded
                        }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                /* Filter */
                                filterExpanded = !filterExpanded
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                    modifier = Modifier
                )
            }
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
                value = searchQuery,
                onValueChange = {
                    viewModel.updateSearchQuery(
                        newQuery = it
                    )
                }
            )
            ConversationList()
            MenuDropdown(
                sortExpanded,
                { sortExpanded = false }
            )
            MenuDropdown(
                filterExpanded,
                { filterExpanded = false }
            )
            NewConversationButton()
        }
    }
}

/*** Create conversation button ***/
@Composable
fun NewConversationButton() {
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


