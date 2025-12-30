package com.boldchallenge.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Search Route (Container)
 * 
 * Connects ViewModel to SearchScreen. Handles state observation and event delegation.
 * No UI logic - delegates all rendering to SearchScreen.
 */
@Composable
fun SearchRoute(
    onLocationSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChanged,
        onRetry = viewModel::retry,
        onLocationSelected = onLocationSelected,
        modifier = modifier
    )
}

