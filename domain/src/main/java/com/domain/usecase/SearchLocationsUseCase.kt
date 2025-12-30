package com.domain.usecase

import com.domain.model.Location
import com.domain.repository.LocationRepository
import com.domain.result.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Use case for searching locations.
 * 
 * Validates minimum query length as defense in depth.
 * Note: ViewModel also validates for immediate UI feedback;
 * this validation prevents unnecessary repository calls.
 */
class SearchLocationsUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(query: String): Flow<ResultState<List<Location>>> {
        // Defense in depth: validate query length before repository call
        // ViewModel validates earlier for immediate UI feedback
        return if (query.length < 2) {
            flowOf(ResultState.Success(emptyList()))
        } else {
            repository.searchLocations(query)
        }
    }
}
