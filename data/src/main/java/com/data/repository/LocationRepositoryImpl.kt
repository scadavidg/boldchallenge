package com.data.repository

import com.data.api.WeatherApi
import com.data.db.LocationDao
import com.data.mapper.toDomain
import com.data.mapper.toEntity
import com.domain.model.Location
import com.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class LocationRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val locationDao: LocationDao,
    @Named("WeatherApiKey") private val apiKey: String
) : LocationRepository {

    // Scope para operaciones en background que no deben cancelarse con el Flow
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun searchLocations(query: String): Flow<List<Location>> {
        return locationDao.searchByQuery(query)
            .map { entities -> entities.map { it.toDomain() } }
            .onStart {
                // Trigger refresh en background sin bloquear la emisión del cache
                refreshFromNetworkInBackground(query)
            }
    }

    private fun refreshFromNetworkInBackground(query: String) {
        backgroundScope.launch {
            try {
                val dtos = weatherApi.searchLocations(apiKey, query)
                val entities = dtos.map { it.toEntity(query) }

                // Limpiar cache anterior para esta query y guardar nuevos resultados
                // Room emitirá automáticamente el nuevo estado a través del Flow
                locationDao.clearByQuery(query)
                locationDao.insertAll(entities)
            } catch (e: Exception) {
                // Manejar errores de red sin romper el flujo del cache
                // El cache seguirá emitiendo sus datos
            }
        }
    }
}

