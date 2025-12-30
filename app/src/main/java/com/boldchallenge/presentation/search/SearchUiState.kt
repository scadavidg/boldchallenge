package com.boldchallenge.presentation.search

import com.domain.error.AppError
import com.domain.model.Location

/**
 * UI state for the Search screen.
 * Immutable data class representing all possible states.
 */
data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val locations: List<Location> = emptyList(),
    val error: AppError? = null,
    val isEmpty: Boolean = false
) {
    /** Show message when query is too short (less than 2 characters) */
    val showQueryTooShort: Boolean
        get() = query.length == 1 && !isLoading && error == null

    /** Show empty state when search completed with no results */
    val showEmptyState: Boolean
        get() = !isLoading && query.length >= 2 && locations.isEmpty() && error == null

    /** Show error only when there's no cached data to display */
    val showError: Boolean
        get() = error != null && locations.isEmpty()
}

