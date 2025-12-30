package com.boldchallenge.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.error.AppError
import com.domain.model.Location
import com.domain.result.ResultState
import com.domain.usecase.SearchLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Search screen.
 * Handles location search with debounce and manages UI state.
 * 
 * Query validation strategy:
 * - ViewModel validates MIN_QUERY_LENGTH for UX optimization (immediate UI feedback)
 * - UseCase also validates as defense in depth (prevents unnecessary repository calls)
 * This dual validation ensures optimal user experience while maintaining data layer safety.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchLocationsUseCase: SearchLocationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val queryFlow = MutableStateFlow("")

    init {
        // Debounce search to avoid excessive API calls
        queryFlow
            .debounce(DEBOUNCE_DELAY_MS)
            .onEach { query -> performSearch(query) }
            .catch { exception ->
                _uiState.value = _uiState.value.copy(
                    error = AppError.UnknownError(exception.message ?: "Unknown error"),
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * Handle query text changes from UI.
     * Validates query length for immediate UI feedback before debounce triggers.
     * Note: UseCase also validates query length as defense in depth.
     */
    fun onQueryChanged(query: String) {
        queryFlow.value = query
        _uiState.value = _uiState.value.copy(query = query, error = null)

        // Immediate UI optimization: clear results for short queries before debounce
        // This provides instant feedback while UseCase validation prevents repository calls
        if (query.length < MIN_QUERY_LENGTH) {
            cancelSearch()
            _uiState.value = _uiState.value.copy(
                locations = emptyList(),
                isLoading = false,
                isEmpty = false
            )
        }
    }

    /** Retry last search after an error */
    fun retry() {
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotEmpty()) {
            performSearch(currentQuery)
        }
    }

    /** Clear current error state */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Perform search with query validation.
     * ViewModel-level validation for immediate state updates;
     * UseCase validation ensures repository layer safety.
     */
    private fun performSearch(query: String) {
        cancelSearch()

        // ViewModel validation: early return for better UX (immediate state update)
        // UseCase will also validate before repository call (defense in depth)
        if (query.length < MIN_QUERY_LENGTH) {
            _uiState.value = _uiState.value.copy(
                locations = emptyList(),
                isLoading = false,
                isEmpty = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        searchJob = viewModelScope.launch {
            searchLocationsUseCase(query)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = AppError.UnknownError(exception.message ?: "Unknown error"),
                        isLoading = false
                    )
                }
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true,
                                locations = result.data ?: emptyList(),
                                error = null
                            )
                        }
                        is ResultState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                locations = result.data,
                                error = null,
                                isEmpty = result.data.isEmpty()
                            )
                        }
                        is ResultState.Failure -> {
                            // Keep cached data visible on error
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.error
                                // locations are preserved (not cleared)
                            )
                        }
                    }
                }
        }
    }

    private fun cancelSearch() {
        searchJob?.cancel()
        searchJob = null
    }

    companion object {
        private const val DEBOUNCE_DELAY_MS = 500L
        private const val MIN_QUERY_LENGTH = 2
    }
}

