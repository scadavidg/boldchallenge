package com.data.repository

import com.data.api.WeatherApi
import com.data.db.ForecastDao
import com.data.error.toAppError
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.domain.model.Forecast
import com.domain.repository.ForecastRepository
import com.domain.result.ResultState
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ForecastRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val forecastDao: ForecastDao,
    private val moshi: Moshi,
    @Named("WeatherApiKey") private val apiKey: String
) : ForecastRepository {

    // Using SupervisorJob to prevent one failing network request from cancelling others
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getForecast(locationName: String, days: Int): Flow<ResultState<Forecast>> {
        return flow {
            // Get initial cache state (non-blocking)
            val cachedForecast = try {
                forecastDao.getForecast(locationName)
                    .map { entity -> entity?.toDomain(moshi) }
                    .catch { emit(null) }
                    .firstOrNull()
            } catch (e: Exception) {
                null
            }

            // Emit Loading with cached data if available
            if (cachedForecast != null) {
                emit(ResultState.Loading(data = cachedForecast))
            } else {
                emit(ResultState.Loading())
            }

            // Channel to receive network result
            val networkResultChannel = Channel<ResultState<Forecast>>(Channel.UNLIMITED)

            // Trigger background refresh
            backgroundScope.launch {
                try {
                    val response = weatherApi.getForecast(apiKey, locationName, days)
                    val entity = response.toEntity(moshi, locationName)
                    val forecast = response.toDomain()

                    // Update cache - Room will automatically emit updated data
                    forecastDao.upsertForecast(entity)

                    // Emit success with fresh data
                    networkResultChannel.send(ResultState.Success(forecast))
                } catch (e: Exception) {
                    val error = e.toAppError()
                    // If we have cached data, emit success with cache (silent fallback)
                    // Otherwise, emit failure
                    if (cachedForecast != null) {
                        networkResultChannel.send(ResultState.Success(cachedForecast))
                    } else {
                        networkResultChannel.send(ResultState.Failure(error))
                    }
                } finally {
                    networkResultChannel.close()
                }
            }

            // Emit network result when available
            networkResultChannel.receiveAsFlow().collect { resultState ->
                emit(resultState)
            }
        }
    }
}

