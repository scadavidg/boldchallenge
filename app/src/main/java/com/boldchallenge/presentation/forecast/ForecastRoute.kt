package com.boldchallenge.presentation.forecast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Forecast Route (Container)
 * 
 * Connects ViewModel to ForecastScreen. Handles state observation and event delegation.
 * No UI logic - delegates all rendering to ForecastScreen.
 */
@Composable
fun ForecastRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForecastViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForecastScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onRefresh = viewModel::refresh,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

