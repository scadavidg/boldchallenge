package com.data.repository

import com.data.api.WeatherApi
import com.data.db.LocationDao
import com.data.error.toAppError
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.domain.model.Location
import com.domain.repository.LocationRepository
import com.domain.result.ResultState
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

class LocationRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val locationDao: LocationDao,
    @Named("WeatherApiKey") private val apiKey: String
) : LocationRepository {

    // Using SupervisorJob to prevent one failing network request from cancelling others
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun searchLocations(query: String): Flow<ResultState<List<Location>>> {
        return flow {
            // Get initial cache state (non-blocking)
            val cachedData = try {
                locationDao.searchByQuery(query)
                    .map { entities -> entities.map { it.toDomain() } }
                    .catch { emit(emptyList()) }
                    .firstOrNull() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            // Emit Loading with cached data if available
            if (cachedData.isNotEmpty()) {
                emit(ResultState.Loading(data = cachedData))
            } else {
                emit(ResultState.Loading())
            }

            // Channel to receive network result
            val networkResultChannel = Channel<ResultState<List<Location>>>(Channel.UNLIMITED)

            // Trigger background refresh
            backgroundScope.launch {
                try {
                    val dtos = weatherApi.searchLocations(apiKey, query)
                    val entities = dtos.map { it.toEntity(query) }
                    val locations = entities.map { it.toDomain() }

                    // Update cache - Room will automatically emit updated data
                    locationDao.clearByQuery(query)
                    locationDao.insertAll(entities)

                    // Emit success with fresh data
                    networkResultChannel.send(ResultState.Success(locations))
                } catch (e: Exception) {
                    val error = e.toAppError()
                    // If we have cached data, emit success with cache (silent fallback)
                    // Otherwise, emit failure
                    if (cachedData.isNotEmpty()) {
                        networkResultChannel.send(ResultState.Success(cachedData))
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

