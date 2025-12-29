package com.domain.repository

import com.domain.model.Location
import com.domain.result.ResultState
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun searchLocations(query: String): Flow<ResultState<List<Location>>>
}
