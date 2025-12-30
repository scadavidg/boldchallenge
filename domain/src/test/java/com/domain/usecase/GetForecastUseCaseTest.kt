package com.domain.usecase

import app.cash.turbine.test
import com.domain.error.AppError
import com.domain.model.Forecast
import com.domain.model.ForecastDay
import com.domain.repository.ForecastRepository
import com.domain.result.ResultState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GetForecastUseCase Tests")
class GetForecastUseCaseTest {

    private lateinit var repository: ForecastRepository
    private lateinit var useCase: GetForecastUseCase

    private val locationName = "Bogotá"
    private val days = 3

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = GetForecastUseCase(repository)
    }

    @Nested
    @DisplayName("Repository Delegation")
    inner class RepositoryDelegationTest {

        @Test
        @DisplayName("GIVEN a location name and days WHEN invoking use case THEN should call repository with correct parameters")
        fun `given location name and days when invoking use case then calls repository with correct parameters`() =
            runTest {
                // Given
                val forecast = createForecast(locationName)
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Success(forecast)
                )

                // When
                useCase(locationName, days).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify(exactly = 1) { repository.getForecast(locationName, days) }
            }

        @Test
        @DisplayName("GIVEN only location name WHEN invoking use case THEN should use default days parameter")
        fun `given only location name when invoking use case then uses default days parameter`() =
            runTest {
                // Given
                val forecast = createForecast(locationName)
                coEvery { repository.getForecast(locationName, 3) } returns flowOf(
                    ResultState.Success(forecast)
                )

                // When
                useCase(locationName).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify(exactly = 1) { repository.getForecast(locationName, 3) }
            }

        @Test
        @DisplayName("GIVEN custom days parameter WHEN invoking use case THEN should pass custom days to repository")
        fun `given custom days parameter when invoking use case then passes custom days to repository`() =
            runTest {
                // Given
                val customDays = 7
                val forecast = createForecast(locationName)
                coEvery { repository.getForecast(locationName, customDays) } returns flowOf(
                    ResultState.Success(forecast)
                )

                // When
                useCase(locationName, customDays).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify(exactly = 1) { repository.getForecast(locationName, customDays) }
            }
    }

    @Nested
    @DisplayName("Success Scenarios")
    inner class SuccessScenariosTest {

        @Test
        @DisplayName("GIVEN repository returns Success WHEN invoking use case THEN should return Success with forecast")
        fun `given repository returns Success when invoking use case then returns Success with forecast`() =
            runTest {
                // Given
                val forecast = createForecast(locationName)
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Success(forecast)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    val success = result as ResultState.Success<Forecast>
                    assertEquals(forecast, success.data)
                    assertEquals(locationName, success.data.locationName)
                    assertEquals(2, success.data.days.size)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN repository returns Success with multiple days WHEN invoking use case THEN should return all days")
        fun `given repository returns Success with multiple days when invoking use case then returns all days`() =
            runTest {
                // Given
                val forecast = createForecastWithMultipleDays(locationName)
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Success(forecast)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    val success = result as ResultState.Success<Forecast>
                    assertEquals(3, success.data.days.size)

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Loading Scenarios")
    inner class LoadingScenariosTest {

        @Test
        @DisplayName("GIVEN repository returns Loading WHEN invoking use case THEN should return Loading state")
        fun `given repository returns Loading when invoking use case then returns Loading state`() =
            runTest {
                // Given
                val cachedForecast = createForecast(locationName)
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Loading(cachedForecast)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Loading)
                    val loading = result as ResultState.Loading<Forecast>
                    assertEquals(cachedForecast, loading.data)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN repository returns Loading without data WHEN invoking use case THEN should return Loading with null data")
        fun `given repository returns Loading without data when invoking use case then returns Loading with null data`() =
            runTest {
                // Given
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Loading<Forecast>(null)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Loading)
                    val loading = result as ResultState.Loading<Forecast>
                    assertEquals(null, loading.data)

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Error Scenarios")
    inner class ErrorScenariosTest {

        @Test
        @DisplayName("GIVEN repository returns Failure WHEN invoking use case THEN should return Failure with error")
        fun `given repository returns Failure when invoking use case then returns Failure with error`() =
            runTest {
                // Given
                val error = AppError.NetworkError.Timeout
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Failure(error)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Failure)
                    val failure = result as ResultState.Failure
                    assertEquals(error, failure.error)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN repository returns HttpError WHEN invoking use case THEN should return Failure with HttpError")
        fun `given repository returns HttpError when invoking use case then returns Failure with HttpError`() =
            runTest {
                // Given
                val error = AppError.NetworkError.HttpError(404, "Not Found")
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Failure(error)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Failure)
                    val failure = result as ResultState.Failure
                    assertTrue(failure.error is AppError.NetworkError.HttpError)
                    val httpError = failure.error as AppError.NetworkError.HttpError
                    assertEquals(404, httpError.code)
                    assertEquals("Not Found", httpError.message)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN repository returns ParseError WHEN invoking use case THEN should return Failure with ParseError")
        fun `given repository returns ParseError when invoking use case then returns Failure with ParseError`() =
            runTest {
                // Given
                val error = AppError.ParseError("Invalid JSON format")
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Failure(error)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Failure)
                    val failure = result as ResultState.Failure
                    assertTrue(failure.error is AppError.ParseError)

                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Flow Behavior")
    inner class FlowBehaviorTest {

        @Test
        @DisplayName("GIVEN repository emits multiple states WHEN invoking use case THEN should emit all states in order")
        fun `given repository emits multiple states when invoking use case then emits all states in order`() =
            runTest {
                // Given
                val cachedForecast = createForecast(locationName)
                val freshForecast = createForecastWithMultipleDays(locationName)

                coEvery {
                    repository.getForecast(locationName, days)
                } returns flowOf(
                    ResultState.Loading(cachedForecast),
                    ResultState.Success(freshForecast)
                )

                // When
                useCase(locationName, days).test {
                    // Then
                    val first = awaitItem()
                    assertTrue(first is ResultState.Loading)

                    val second = awaitItem()
                    assertTrue(second is ResultState.Success)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN successful invocation WHEN invoking use case THEN should close flow after completion")
        fun `given successful invocation when invoking use case then closes flow after completion`() =
            runTest {
                // Given
                val forecast = createForecast(locationName)
                coEvery { repository.getForecast(locationName, days) } returns flowOf(
                    ResultState.Success(forecast)
                )

                // When
                useCase(locationName, days).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then - test completes successfully (flow was closed)
            }
    }

    // Helper methods
    private fun createForecast(locationName: String): Forecast {
        return Forecast(
            locationName = locationName,
            days = listOf(
                ForecastDay(
                    date = "2024-01-15",
                    avgTempC = 15.5,
                    conditionText = "Sunny",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/113.png"
                ),
                ForecastDay(
                    date = "2024-01-16",
                    avgTempC = 18.0,
                    conditionText = "Cloudy",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
                )
            )
        )
    }

    private fun createForecastWithMultipleDays(locationName: String): Forecast {
        return Forecast(
            locationName = locationName,
            days = listOf(
                ForecastDay(
                    date = "2024-01-15",
                    avgTempC = 15.5,
                    conditionText = "Sunny",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/113.png"
                ),
                ForecastDay(
                    date = "2024-01-16",
                    avgTempC = 18.0,
                    conditionText = "Cloudy",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
                ),
                ForecastDay(
                    date = "2024-01-17",
                    avgTempC = 20.0,
                    conditionText = "Clear",
                    conditionIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/113.png"
                )
            )
        )
    }
}

