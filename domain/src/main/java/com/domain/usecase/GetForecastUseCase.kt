package com.domain.usecase

import com.domain.model.Forecast
import com.domain.repository.ForecastRepository
import com.domain.result.ResultState
import kotlinx.coroutines.flow.Flow

class GetForecastUseCase(
    private val repository: ForecastRepository
) {
    operator fun invoke(locationName: String, days: Int = 3): Flow<ResultState<Forecast>> {
        return repository.getForecast(locationName, days)
    }
}

