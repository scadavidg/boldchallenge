package com.domain.repository

import com.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun searchLocations(query: String): Flow<List<Location>>
}
