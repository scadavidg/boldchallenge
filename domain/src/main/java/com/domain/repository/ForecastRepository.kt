package com.domain.repository

import com.domain.model.Forecast
import com.domain.result.ResultState
import kotlinx.coroutines.flow.Flow

interface ForecastRepository {
    fun getForecast(locationName: String, days: Int = 3): Flow<ResultState<Forecast>>
}

