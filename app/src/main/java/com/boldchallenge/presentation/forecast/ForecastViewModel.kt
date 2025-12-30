package com.boldchallenge.presentation.forecast

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.error.AppError
import com.domain.result.ResultState
import com.domain.usecase.GetForecastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * ViewModel for the Forecast screen.
 * Fetches weather forecast data and manages UI state with cache-first strategy.
 */
@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val getForecastUseCase: GetForecastUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val locationName: String = savedStateHandle.get<String>("locationName") ?: ""

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init {
        loadForecast()
    }

    /** Retry loading forecast after an error */
    fun retry() {
        loadForecast()
    }

    /** Refresh forecast data while keeping existing data visible */
    fun refresh() {
        loadForecast(showRefreshing = _uiState.value.forecast != null)
    }

    private fun loadForecast(showRefreshing: Boolean = false) {
        if (locationName.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = AppError.UnknownError("Location name is required"),
                isLoading = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = _uiState.value.forecast == null && !showRefreshing,
            isRefreshing = showRefreshing,
            error = null
        )

        getForecastUseCase(locationName, days = 3)
            .catch { exception ->
                _uiState.value = _uiState.value.copy(
                    error = AppError.UnknownError(exception.message ?: "Unknown error"),
                    isLoading = false,
                    isRefreshing = false
                )
            }
            .onEach { result ->
                when (result) {
                    is ResultState.Loading -> {
                        // Show cached data while loading fresh data
                        _uiState.value = _uiState.value.copy(
                            isLoading = result.data == null,
                            isRefreshing = result.data != null,
                            forecast = result.data,
                            error = null
                        )
                    }
                    is ResultState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            forecast = result.data,
                            error = null
                        )
                    }
                    is ResultState.Failure -> {
                        // Keep cached data visible on error
                        if (_uiState.value.forecast == null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.error
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isRefreshing = false,
                                error = result.error
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}

