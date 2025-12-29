package com.domain.usecase

import com.domain.model.Location
import com.domain.repository.LocationRepository
import com.domain.result.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SearchLocationsUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(query: String): Flow<ResultState<List<Location>>> {
        // Minimum 2 characters to avoid excessive API calls and improve UX
        return if (query.length < 2) {
            flowOf(ResultState.Success(emptyList()))
        } else {
            repository.searchLocations(query)
        }
    }
}
