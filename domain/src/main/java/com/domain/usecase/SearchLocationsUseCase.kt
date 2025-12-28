package com.domain.usecase

import com.domain.model.Location
import com.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SearchLocationsUseCase(
    private val repository: LocationRepository
) {
    operator fun invoke(query: String): Flow<List<Location>> {
        return if (query.length < 2) {
            flowOf(emptyList())
        } else {
            repository.searchLocations(query)
        }
    }
}
