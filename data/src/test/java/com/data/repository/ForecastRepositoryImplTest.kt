package com.data.repository

import app.cash.turbine.test
import com.data.api.WeatherApi
import com.data.db.ForecastDao
import com.data.db.ForecastEntity
import com.data.dto.ConditionDto
import com.data.dto.DayDto
import com.data.dto.ForecastDayDto
import com.data.dto.ForecastDto
import com.data.dto.ForecastResponseDto
import com.data.dto.LocationDto
import com.domain.error.AppError
import com.domain.model.Forecast
import com.domain.result.ResultState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

@DisplayName("ForecastRepositoryImpl Tests")
class ForecastRepositoryImplTest {

    private lateinit var weatherApi: WeatherApi
    private lateinit var forecastDao: ForecastDao
    private lateinit var moshi: Moshi
    private lateinit var repository: ForecastRepositoryImpl

    private val apiKey = "test-api-key"
    private val locationName = "Bogotá"

    @BeforeEach
    fun setup() {
        weatherApi = mockk(relaxed = true)
        forecastDao = mockk(relaxed = true)
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        repository = ForecastRepositoryImpl(
            weatherApi = weatherApi,
            forecastDao = forecastDao,
            moshi = moshi,
            apiKey = apiKey
        )
    }

    @Nested
    @DisplayName("Cache Scenarios")
    inner class CacheScenariosTest {

        @Test
        @DisplayName("GIVEN cache exists WHEN getting forecast THEN should emit Loading with cached data first")
        fun `given cache exists when getting forecast then emits Loading with cached data first`() =
            runTest {
                // Given
                val cachedEntity = createForecastEntity(locationName, moshi)
                val cachedForecast = createForecast(locationName)

                every { forecastDao.getForecast(locationName) } returns flowOf(cachedEntity)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } returns createForecastResponseDto(locationName)

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    val firstEmission = awaitItem()
                    assertTrue(firstEmission is ResultState.Loading)
                    val loading = firstEmission as ResultState.Loading<Forecast>
                    assertEquals(cachedForecast, loading.data)

                    // Wait for success
                    val secondEmission = awaitItem()
                    assertTrue(secondEmission is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN no cache exists WHEN getting forecast THEN should emit Loading without data")
        fun `given no cache exists when getting forecast then emits Loading without data`() =
            runTest {
                // Given
                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } returns createForecastResponseDto(locationName)

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    val firstEmission = awaitItem()
                    assertTrue(firstEmission is ResultState.Loading)
                    assertEquals(null, (firstEmission as ResultState.Loading).data)

                    // Wait for success
                    val secondEmission = awaitItem()
                    assertTrue(secondEmission is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN cache deserialization fails WHEN getting forecast THEN should emit Loading without data")
        fun `given cache deserialization fails when getting forecast then emits Loading without data`() =
            runTest {
                // Given
                val invalidEntity = ForecastEntity(
                    locationName = locationName,
                    serializedForecast = "invalid json",
                    lastUpdated = System.currentTimeMillis()
                )

                every { forecastDao.getForecast(locationName) } returns flowOf(invalidEntity)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } returns createForecastResponseDto(locationName)

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    val firstEmission = awaitItem()
                    assertTrue(firstEmission is ResultState.Loading)
                    assertEquals(null, (firstEmission as ResultState.Loading).data)

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
        @DisplayName("GIVEN network request succeeds WHEN getting forecast THEN should emit Success with fresh data and update cache")
        fun `given network request succeeds when getting forecast then emits Success with fresh data and updates cache`() =
            runTest {
                // Given
                val responseDto = createForecastResponseDto(locationName)

                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery { weatherApi.getForecast(apiKey, locationName, 3) } returns responseDto

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    val loading = awaitItem()
                    assertTrue(loading is ResultState.Loading)

                    val success = awaitItem()
                    assertTrue(success is ResultState.Success)
                    val successState = success as ResultState.Success<Forecast>
                    assertEquals(locationName, successState.data.locationName)
                    assertEquals(1, successState.data.days.size)

                    coVerify { forecastDao.upsertForecast(any()) }

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request succeeds with multiple days WHEN getting forecast THEN should return all days")
        fun `given network request succeeds with multiple days when getting forecast then returns all days`() =
            runTest {
                // Given
                val responseDto = createForecastResponseDtoWithMultipleDays(locationName)

                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery { weatherApi.getForecast(apiKey, locationName, 3) } returns responseDto

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    awaitItem() // Loading

                    val success = awaitItem()
                    assertTrue(success is ResultState.Success)
                    val successState = success as ResultState.Success<Forecast>
                    assertEquals(3, successState.data.days.size)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request succeeds WHEN getting forecast THEN should update cache with correct entity")
        fun `given network request succeeds when getting forecast then updates cache with correct entity`() =
            runTest {
                // Given
                val responseDto = createForecastResponseDto(locationName)

                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery { weatherApi.getForecast(apiKey, locationName, 3) } returns responseDto

                // When
                repository.getForecast(locationName, 3).test {
                    awaitItem() // Loading
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify { forecastDao.upsertForecast(any<ForecastEntity>()) }
            }
    }

    @Nested
    @DisplayName("Network Error Scenarios")
    inner class NetworkErrorScenariosTest {

        @Test
        @DisplayName("GIVEN network request fails and cache exists WHEN getting forecast THEN should emit Success with cached data")
        fun `given network request fails and cache exists when getting forecast then emits Success with cached data`() =
            runTest {
                // Given
                val cachedEntity = createForecastEntity(locationName, moshi)
                val cachedForecast = createForecast(locationName)

                every { forecastDao.getForecast(locationName) } returns flowOf(cachedEntity)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } throws SocketTimeoutException("Request timed out")

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    val loading = awaitItem()
                    assertTrue(loading is ResultState.Loading)
                    val loadingState = loading as ResultState.Loading<Forecast>
                    assertEquals(cachedForecast, loadingState.data)

                    val success = awaitItem()
                    assertTrue(success is ResultState.Success)
                    val successState = success as ResultState.Success<Forecast>
                    assertEquals(cachedForecast, successState.data)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN network request fails and no cache exists WHEN getting forecast THEN should emit Failure with error")
        fun `given network request fails and no cache exists when getting forecast then emits Failure with error`() =
            runTest {
                // Given
                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } throws SocketTimeoutException("Request timed out")

                // When
                repository.getForecast(locationName, 3).test {
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
        @DisplayName("GIVEN network request fails with HttpException WHEN getting forecast without cache THEN should emit Failure with HttpError")
        fun `given network request fails with HttpException when getting forecast without cache then emits Failure with HttpError`() =
            runTest {
                // Given
                val httpException = retrofit2.HttpException(
                    retrofit2.Response.error<String>(404, "Not Found".toResponseBody(null))
                )

                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery { weatherApi.getForecast(apiKey, locationName, 3) } throws httpException

                // When
                repository.getForecast(locationName, 3).test {
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
        @DisplayName("GIVEN network request fails with ParseError WHEN getting forecast without cache THEN should emit Failure with ParseError")
        fun `given network request fails with ParseError when getting forecast without cache then emits Failure with ParseError`() =
            runTest {
                // Given
                val parseException = com.squareup.moshi.JsonEncodingException("Malformed JSON")

                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery { weatherApi.getForecast(apiKey, locationName, 3) } throws parseException

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    awaitItem() // Loading

                    val failure = awaitItem()
                    assertTrue(failure is ResultState.Failure)
                    val failureState = failure as ResultState.Failure
                    assertTrue(failureState.error is AppError.ParseError)

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Flow Behavior")
    inner class FlowBehaviorTest {

        @Test
        @DisplayName("GIVEN successful request WHEN getting forecast THEN should emit states in correct order")
        fun `given successful request when getting forecast then emits states in correct order`() =
            runTest {
                // Given
                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } returns createForecastResponseDto(locationName)

                // When
                repository.getForecast(locationName, 3).test {
                    // Then
                    val first = awaitItem()
                    assertTrue(first is ResultState.Loading)

                    val second = awaitItem()
                    assertTrue(second is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN successful request WHEN getting forecast THEN should close channel after completion")
        fun `given successful request when getting forecast then closes channel after completion`() =
            runTest {
                // Given
                every { forecastDao.getForecast(locationName) } returns flowOf(null)
                coEvery {
                    weatherApi.getForecast(
                        apiKey,
                        locationName,
                        3
                    )
                } returns createForecastResponseDto(locationName)

                // When
                repository.getForecast(locationName, 3).test {
                    awaitItem() // Loading
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then - test completes successfully (channel was closed)
            }
    }

    // Helper methods
    private fun createForecastResponseDto(locationName: String): ForecastResponseDto {
        return ForecastResponseDto(
            location = LocationDto(
                id = 1L,
                name = locationName,
                region = "Region",
                country = "Country",
                lat = 0.0,
                lon = 0.0,
                url = locationName.lowercase()
            ),
            forecast = ForecastDto(
                forecastday = listOf(
                    ForecastDayDto(
                        date = "2024-01-15",
                        day = DayDto(
                            avgTempC = 15.5,
                            condition = ConditionDto(
                                text = "Sunny",
                                icon = "//cdn.weatherapi.com/weather/64x64/day/113.png"
                            )
                        )
                    )
                )
            )
        )
    }

    private fun createForecastResponseDtoWithMultipleDays(locationName: String): ForecastResponseDto {
        return ForecastResponseDto(
            location = LocationDto(
                id = 1L,
                name = locationName,
                region = "Region",
                country = "Country",
                lat = 0.0,
                lon = 0.0,
                url = locationName.lowercase()
            ),
            forecast = ForecastDto(
                forecastday = listOf(
                    ForecastDayDto(
                        date = "2024-01-15",
                        day = DayDto(
                            avgTempC = 15.5,
                            condition = ConditionDto(
                                text = "Sunny",
                                icon = "//cdn.weatherapi.com/weather/64x64/day/113.png"
                            )
                        )
                    ),
                    ForecastDayDto(
                        date = "2024-01-16",
                        day = DayDto(
                            avgTempC = 18.0,
                            condition = ConditionDto(
                                text = "Cloudy",
                                icon = "//cdn.weatherapi.com/weather/64x64/day/116.png"
                            )
                        )
                    ),
                    ForecastDayDto(
                        date = "2024-01-17",
                        day = DayDto(
                            avgTempC = 20.0,
                            condition = ConditionDto(
                                text = "Clear",
                                icon = "//cdn.weatherapi.com/weather/64x64/day/113.png"
                            )
                        )
                    )
                )
            )
        )
    }

    private fun createForecastEntity(locationName: String, moshi: Moshi): ForecastEntity {
        val responseDto = createForecastResponseDto(locationName)
        val adapter = moshi.adapter(ForecastResponseDto::class.java)
        val json = adapter.toJson(responseDto)
        return ForecastEntity(
            locationName = locationName,
            serializedForecast = json,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun createForecast(locationName: String): Forecast {
        return Forecast(
            locationName = locationName,
            days = listOf(
                com.domain.model.ForecastDay(
                    date = "2024-01-15",
                    avgTempC = 15.5,
                    conditionText = "Sunny",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/113.png"
                )
            )
        )
    }
}
