package com.data.repository

import app.cash.turbine.test
import com.data.api.WeatherApi
import com.data.db.LocationDao
import com.data.db.LocationEntity
import com.data.dto.LocationDto
import com.data.mapper.toDomain
import com.domain.error.AppError
import com.domain.model.Location
import com.domain.result.ResultState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.SocketTimeoutException

@DisplayName("LocationRepositoryImpl Tests")
class LocationRepositoryImplTest {

    private lateinit var weatherApi: WeatherApi
    private lateinit var locationDao: LocationDao
    private lateinit var repository: LocationRepositoryImpl

    private val apiKey = "test-api-key"
    private val query = "bogota"

    @BeforeEach
    fun setup() {
        weatherApi = mockk(relaxed = true)
        locationDao = mockk(relaxed = true)

        repository = LocationRepositoryImpl(
            weatherApi = weatherApi,
            locationDao = locationDao,
            apiKey = apiKey
        )
    }

    @Nested
    @DisplayName("Cache Scenarios")
    inner class CacheScenariosTest {

        @Test
        @DisplayName("GIVEN cache exists WHEN searching locations THEN should emit Loading with cached data first")
        fun `given cache exists when searching locations then emits Loading with cached data first`() =
            runTest {
                // Given
                val cachedEntities = listOf(
                    createLocationEntity(1L, "Bogotá", query),
                    createLocationEntity(2L, "Bogotá D.C.", query)
                )
                val cachedLocations = cachedEntities.map { entity -> entity.toDomain() }

                every { locationDao.searchByQuery(query) } returns flowOf(cachedEntities)
                coEvery { weatherApi.searchLocations(apiKey, query) } returns createLocationDtos()

                // When
                repository.searchLocations(query).test {
                    // Then
                    val firstEmission = awaitItem()
                    assertTrue(firstEmission is ResultState.Loading)
                    val loading = firstEmission as ResultState.Loading<List<Location>>
                    assertEquals(cachedLocations, loading.data)

                    // Wait for success
                    val secondEmission = awaitItem()
                    assertTrue(secondEmission is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN no cache exists WHEN searching locations THEN should emit Loading without data")
        fun `given no cache exists when searching locations then emits Loading without data`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } returns createLocationDtos()

                // When
                repository.searchLocations(query).test {
                    // Then
                    val firstEmission = awaitItem()
                    assertTrue(firstEmission is ResultState.Loading)
                    val loading = firstEmission as ResultState.Loading<List<Location>>
                    // When cachedData is empty list, isNotEmpty() is false, so Loading is emitted without data (null)
                    assertEquals(null, loading.data)

                    // Wait for success
                    val secondEmission = awaitItem()
                    assertTrue(secondEmission is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN cache query fails WHEN searching locations THEN should emit Loading without data")
        fun `given cache query fails when searching locations then emits Loading without data`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } throws RuntimeException("Database error")
                coEvery { weatherApi.searchLocations(apiKey, query) } returns createLocationDtos()

                // When
                repository.searchLocations(query).test {
                    // Then
                    val firstEmission = awaitItem()
                    assertTrue(firstEmission is ResultState.Loading)
                    val loading = firstEmission as ResultState.Loading<List<Location>>
                    // When exception is caught, cachedData is emptyList(), which means Loading is emitted without data (null)
                    assertEquals(null, loading.data)

                    // Wait for success
                    val secondEmission = awaitItem()
                    assertTrue(secondEmission is ResultState.Success)

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Network Success Scenarios")
    inner class NetworkSuccessScenariosTest {

        @Test
        @DisplayName("GIVEN network request succeeds WHEN searching locations THEN should emit Success with locations and update cache")
        fun `given network request succeeds when searching locations then emits Success with locations and updates cache`() =
            runTest {
                // Given
                val locationDtos = createLocationDtos()

                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } returns locationDtos

                // When
                repository.searchLocations(query).test {
                    // Then
                    val loading = awaitItem()
                    assertTrue(loading is ResultState.Loading)

                    val success = awaitItem()
                    assertTrue(success is ResultState.Success)
                    val successState = success as ResultState.Success<List<Location>>
                    assertEquals(2, successState.data.size)
                    assertEquals("Bogotá", successState.data[0].name)
                    assertEquals("Bogotá D.C.", successState.data[1].name)

                    coVerify { locationDao.clearByQuery(query) }
                    coVerify { locationDao.insertAll(any()) }

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request succeeds WHEN searching locations THEN should clear old cache and insert new entities")
        fun `given network request succeeds when searching locations then clears old cache and inserts new entities`() =
            runTest {
                // Given
                val locationDtos = createLocationDtos()

                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } returns locationDtos

                // When
                repository.searchLocations(query).test {
                    awaitItem() // Loading
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify { locationDao.clearByQuery(query) }
                coVerify { locationDao.insertAll(any<List<LocationEntity>>()) }
            }

        @Test
        @DisplayName("GIVEN network request succeeds with empty results WHEN searching locations THEN should emit Success with empty list")
        fun `given network request succeeds with empty results when searching locations then emits Success with empty list`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } returns emptyList()

                // When
                repository.searchLocations(query).test {
                    // Then
                    awaitItem() // Loading

                    val success = awaitItem()
                    assertTrue(success is ResultState.Success)
                    val successState = success as ResultState.Success<List<Location>>
                    assertTrue(successState.data.isEmpty())

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Network Error Scenarios")
    inner class NetworkErrorScenariosTest {

        @Test
        @DisplayName("GIVEN network request fails and cache exists WHEN searching locations THEN should emit Success with cached data")
        fun `given network request fails and cache exists when searching locations then emits Success with cached data`() =
            runTest {
                // Given
                val cachedEntities = listOf(
                    createLocationEntity(1L, "Bogotá", query),
                    createLocationEntity(2L, "Bogotá D.C.", query)
                )
                val cachedLocations = cachedEntities.map { entity -> entity.toDomain() }

                every { locationDao.searchByQuery(query) } returns flowOf(cachedEntities)
                coEvery { weatherApi.searchLocations(apiKey, query) } throws SocketTimeoutException(
                    "Request timed out"
                )

                // When
                repository.searchLocations(query).test {
                    // Then
                    val loading = awaitItem()
                    assertTrue(loading is ResultState.Loading)
                    val loadingState = loading as ResultState.Loading<List<Location>>
                    assertEquals(cachedLocations, loadingState.data)

                    val success = awaitItem()
                    assertTrue(success is ResultState.Success)
                    val successState = success as ResultState.Success<List<Location>>
                    assertEquals(cachedLocations, successState.data)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request fails and no cache exists WHEN searching locations THEN should emit Failure with error")
        fun `given network request fails and no cache exists when searching locations then emits Failure with error`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } throws SocketTimeoutException(
                    "Request timed out"
                )

                // When
                repository.searchLocations(query).test {
                    // Then
                    val loading = awaitItem()
                    assertTrue(loading is ResultState.Loading)

                    val failure = awaitItem()
                    assertTrue(failure is ResultState.Failure)
                    val failureState = failure as ResultState.Failure
                    assertTrue(failureState.error is AppError.NetworkError.Timeout)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request fails with HttpException WHEN searching locations without cache THEN should emit Failure with HttpError")
        fun `given network request fails with HttpException when searching locations without cache then emits Failure with HttpError`() =
            runTest {
                // Given
                val httpException = retrofit2.HttpException(
                    retrofit2.Response.error<String>(404, "Not Found".toResponseBody(null))
                )

                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } throws httpException

                // When
                repository.searchLocations(query).test {
                    // Then
                    awaitItem() // Loading

                    val failure = awaitItem()
                    assertTrue(failure is ResultState.Failure)
                    val failureState = failure as ResultState.Failure
                    assertTrue(failureState.error is AppError.NetworkError.HttpError)
                    assertEquals(404, (failureState.error as AppError.NetworkError.HttpError).code)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request fails with empty cache WHEN searching locations THEN should emit Failure")
        fun `given network request fails with empty cache when searching locations then emits Failure`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery {
                    weatherApi.searchLocations(
                        apiKey,
                        query
                    )
                } throws RuntimeException("Unknown error")

                // When
                repository.searchLocations(query).test {
                    // Then
                    awaitItem() // Loading

                    val failure = awaitItem()
                    assertTrue(failure is ResultState.Failure)
                    val failureState = failure as ResultState.Failure
                    assertTrue(failureState.error is AppError.UnknownError)

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Flow Behavior")
    inner class FlowBehaviorTest {

        @Test
        @DisplayName("GIVEN successful request WHEN searching locations THEN should emit states in correct order")
        fun `given successful request when searching locations then emits states in correct order`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } returns createLocationDtos()

                // When
                repository.searchLocations(query).test {
                    // Then
                    val first = awaitItem()
                    assertTrue(first is ResultState.Loading)

                    val second = awaitItem()
                    assertTrue(second is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN successful request WHEN searching locations THEN should close channel after completion")
        fun `given successful request when searching locations then closes channel after completion`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(query) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(apiKey, query) } returns createLocationDtos()

                // When
                repository.searchLocations(query).test {
                    awaitItem() // Loading
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then - test completes successfully (channel was closed)
            }

        @Test
        @DisplayName("GIVEN multiple searches WHEN searching locations THEN should handle each independently")
        fun `given multiple searches when searching locations then handles each independently`() =
            runTest {
                // Given
                every { locationDao.searchByQuery(any()) } returns flowOf(emptyList())
                coEvery { weatherApi.searchLocations(any(), any()) } returns createLocationDtos()

                // When
                repository.searchLocations("bogota").test {
                    awaitItem() // Loading
                    awaitItem() // Success
                    awaitComplete()
                }

                repository.searchLocations("medellin").test {
                    awaitItem() // Loading
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then - both searches complete successfully
            }
    }

    // Helper methods
    private fun createLocationDtos(): List<LocationDto> {
        return listOf(
            LocationDto(
                id = 1L,
                name = "Bogotá",
                region = "Cundinamarca",
                country = "Colombia",
                lat = 4.6097,
                lon = -74.0817,
                url = "bogota-colombia"
            ),
            LocationDto(
                id = 2L,
                name = "Bogotá D.C.",
                region = "Cundinamarca",
                country = "Colombia",
                lat = 4.6097,
                lon = -74.0817,
                url = "bogota-dc-colombia"
            )
        )
    }

    private fun createLocationEntity(id: Long, name: String, query: String): LocationEntity {
        return LocationEntity(
            id = id,
            name = name,
            region = "Cundinamarca",
            country = "Colombia",
            lat = 4.6097,
            lon = -74.0817,
            url = "$name-colombia",
            query = query
        )
    }
}
