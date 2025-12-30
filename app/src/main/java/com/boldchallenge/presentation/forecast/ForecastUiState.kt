package com.boldchallenge.presentation.forecast

import com.domain.error.AppError
import com.domain.model.Forecast

/**
 * UI state for the Forecast screen.
 * Supports cache-first strategy with loading and refreshing states.
 */
data class ForecastUiState(
    val isLoading: Boolean = false,
    val forecast: Forecast? = null,
    val error: AppError? = null,
    val isRefreshing: Boolean = false
) {
    /** True when forecast data is available */
    val hasData: Boolean
        get() = forecast != null

    /** Show error only when there's no cached data to display */
    val showError: Boolean
        get() = error != null && forecast == null

    /** Show loading only on initial load without cached data */
    val showLoading: Boolean
        get() = isLoading && forecast == null
}

