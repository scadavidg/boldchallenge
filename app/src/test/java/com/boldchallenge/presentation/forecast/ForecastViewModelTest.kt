package com.boldchallenge.presentation.forecast

import app.cash.turbine.test
import com.domain.error.AppError
import com.domain.model.Forecast
import com.domain.model.ForecastDay
import com.domain.result.ResultState
import com.domain.usecase.GetForecastUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ForecastViewModel Tests")
class ForecastViewModelTest {

    private lateinit var getForecastUseCase: GetForecastUseCase
    private lateinit var savedStateHandle: androidx.lifecycle.SavedStateHandle

    @BeforeEach
    fun setup() {
        getForecastUseCase = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("Initial Load")
    inner class InitialLoadTest {

        @Test
        @DisplayName(
                "GIVEN valid locationName WHEN ViewModel is created THEN should load forecast automatically"
        )
        fun `given valid locationName when ViewModel is created then loads forecast automatically`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val forecast = createForecast(locationName)
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName
                    coEvery { getForecastUseCase(locationName, 3) } returns
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Success(forecast))
                            }

                    // When
                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    // Then - start listening first, then advance
                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        // So the first state we see is already the loading state
                        val loadingState = awaitItem()
                        assertTrue(
                                loadingState.isLoading,
                                "Expected isLoading to be true but was ${loadingState.isLoading}"
                        )
                        assertTrue(loadingState.showLoading, "Expected showLoading to be true")

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Success state
                        val successState = awaitItem()
                        assertNotNull(successState.forecast)
                        assertEquals(forecast, successState.forecast)
                        assertFalse(successState.isLoading)

                        cancelAndIgnoreRemainingEvents()
                    }

                    coVerify(exactly = 1) { getForecastUseCase(locationName, 3) }
                }

        @Test
        @DisplayName("GIVEN empty locationName WHEN ViewModel is created THEN should show error")
        fun `given empty locationName when ViewModel is created then shows error`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            io.mockk.every { savedStateHandle.get<String>("locationName") } returns ""

            // When
            val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 0) { getForecastUseCase(any(), any()) }
            val state = viewModel.uiState.value
            assertTrue(state.error is AppError.UnknownError)
            assertFalse(state.isLoading)
        }

        @Test
        @DisplayName("GIVEN null locationName WHEN ViewModel is created THEN should show error")
        fun `given null locationName when ViewModel is created then shows error`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            io.mockk.every { savedStateHandle.get<String>("locationName") } returns null

            // When
            val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)
            advanceUntilIdle()

            // Then
            coVerify(exactly = 0) { getForecastUseCase(any(), any()) }
            val state = viewModel.uiState.value
            assertTrue(state.error is AppError.UnknownError)
            assertFalse(state.isLoading)
        }
    }

    @Nested
    @DisplayName("Cache-First Strategy")
    inner class CacheFirstStrategyTest {

        @Test
        @DisplayName(
                "GIVEN Loading with cached data WHEN ResultState.Loading has data THEN should show isRefreshing and keep data visible"
        )
        fun `given Loading with cached data when ResultState Loading has data then shows isRefreshing and keeps data visible`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val cachedForecast = createForecast(locationName)
                    val updatedForecast = createForecast(locationName, "Sunny")
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName
                    coEvery { getForecastUseCase(locationName, 3) } returns
                            flow {
                                emit(ResultState.Loading(cachedForecast))
                                delay(1)
                                emit(ResultState.Success(updatedForecast))
                            }

                    // When
                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    // Then - start listening first, then advance
                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        val initialLoadingState = awaitItem()
                        assertTrue(initialLoadingState.isLoading)

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Flow emits Loading with cached data -> isRefreshing=true
                        val loadingState = awaitItem()
                        assertTrue(loadingState.isRefreshing)
                        assertFalse(loadingState.isLoading)
                        assertEquals(cachedForecast, loadingState.forecast)
                        assertFalse(loadingState.showLoading)

                        advanceUntilIdle()
                        val successState = awaitItem()
                        assertEquals(updatedForecast, successState.forecast)
                        assertFalse(successState.isRefreshing)

                        cancelAndIgnoreRemainingEvents()
                    }
                }

        @Test
        @DisplayName(
                "GIVEN Loading without cached data WHEN ResultState.Loading has no data THEN should show isLoading"
        )
        fun `given Loading without cached data when ResultState Loading has no data then shows isLoading`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val forecast = createForecast(locationName)
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName
                    coEvery { getForecastUseCase(locationName, 3) } returns
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Success(forecast))
                            }

                    // When
                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    // Then - start listening first, then advance
                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        val initialLoadingState = awaitItem()
                        assertTrue(initialLoadingState.isLoading)
                        assertFalse(initialLoadingState.isRefreshing)
                        assertTrue(initialLoadingState.showLoading)
                        assertNull(initialLoadingState.forecast)

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Flow emits Loading() -> same state (conflated, might not emit)
                        // Flow emits Success
                        val successState = awaitItem()
                        assertNotNull(successState.forecast)
                        assertFalse(successState.isLoading)

                        cancelAndIgnoreRemainingEvents()
                    }
                }
    }

    @Nested
    @DisplayName("Success States")
    inner class SuccessStatesTest {

        @Test
        @DisplayName(
                "GIVEN successful load WHEN ResultState.Success is received THEN should update forecast and clear loading states"
        )
        fun `given successful load when ResultState Success is received then updates forecast and clears loading states`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val forecast = createForecast(locationName)
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName
                    coEvery { getForecastUseCase(locationName, 3) } returns
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Success(forecast))
                            }

                    // When
                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    // Then - start listening first, then advance
                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        val initialLoadingState = awaitItem()
                        assertTrue(initialLoadingState.isLoading)

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Flow emits Success
                        val successState = awaitItem()
                        assertFalse(successState.isLoading)
                        assertFalse(successState.isRefreshing)
                        assertNotNull(successState.forecast)
                        assertEquals(forecast, successState.forecast)
                        assertNull(successState.error)
                        assertFalse(successState.showLoading)
                        assertFalse(successState.showError)

                        cancelAndIgnoreRemainingEvents()
                    }
                }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTest {

        @Test
        @DisplayName(
                "GIVEN error without cached data WHEN ResultState.Failure is received THEN should show error"
        )
        fun `given error without cached data when ResultState Failure is received then shows error`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val error = AppError.NetworkError.NoConnection
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName
                    coEvery { getForecastUseCase(locationName, 3) } returns
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Failure(error))
                            }

                    // When
                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    // Then - start listening first, then advance
                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        val initialLoadingState = awaitItem()
                        assertTrue(initialLoadingState.isLoading)

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Flow emits Failure
                        val errorState = awaitItem()
                        assertFalse(errorState.isLoading)
                        assertFalse(errorState.isRefreshing)
                        assertNull(errorState.forecast)
                        assertEquals(error, errorState.error)
                        assertTrue(errorState.showError)

                        cancelAndIgnoreRemainingEvents()
                    }
                }

        @Test
        @DisplayName(
                "GIVEN error with cached data WHEN ResultState.Failure is received THEN should keep data visible and not show error"
        )
        fun `given error with cached data when ResultState Failure is received then keeps data visible and does not show error`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val cachedForecast = createForecast(locationName)
                    val error = AppError.NetworkError.NoConnection
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName
                    coEvery { getForecastUseCase(locationName, 3) } returns
                            flow {
                                emit(ResultState.Loading(cachedForecast))
                                delay(1)
                                emit(ResultState.Failure(error))
                            }

                    // When
                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    // Then - start listening first, then advance
                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        val initialLoadingState = awaitItem()
                        assertTrue(initialLoadingState.isLoading)

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Flow emits Loading with cached data -> isRefreshing=true
                        val loadingState = awaitItem()
                        assertTrue(loadingState.isRefreshing)
                        assertEquals(cachedForecast, loadingState.forecast)

                        advanceUntilIdle()
                        val errorState = awaitItem()
                        assertFalse(errorState.isRefreshing)
                        assertNotNull(errorState.forecast)
                        assertEquals(cachedForecast, errorState.forecast)
                        assertEquals(error, errorState.error)
                        assertFalse(
                                errorState.showError
                        ) // Should not show error when cached data exists

                        cancelAndIgnoreRemainingEvents()
                    }
                }

        @Test
        @DisplayName("GIVEN exception in flow WHEN load fails THEN should map to UnknownError")
        fun `given exception in flow when load fails then maps to UnknownError`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val locationName = "Bogotá"
            val exception = RuntimeException("Network error")
            io.mockk.every { savedStateHandle.get<String>("locationName") } returns locationName
            coEvery { getForecastUseCase(locationName, 3) } returns
                    flow {
                        emit(ResultState.Loading())
                        delay(1)
                        throw exception
                    }

            // When
            val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

            // Then - start listening first, then advance
            viewModel.uiState.test {
                // ViewModel init runs synchronously and sets isLoading=true immediately
                val initialLoadingState = awaitItem()
                assertTrue(initialLoadingState.isLoading)

                // Advance scheduler to start flow collection
                advanceUntilIdle()

                // Flow throws exception -> catch block sets error
                val errorState = awaitItem()
                assertTrue(errorState.error is AppError.UnknownError)
                assertFalse(errorState.isLoading)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Retry Functionality")
    inner class RetryFunctionalityTest {

        @Test
        @DisplayName("GIVEN error state WHEN retry is called THEN should reload forecast")
        fun `given error state when retry is called then reloads forecast`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val locationName = "Bogotá"
            val error = AppError.NetworkError.NoConnection
            val forecast = createForecast(locationName)
            io.mockk.every { savedStateHandle.get<String>("locationName") } returns locationName

            coEvery { getForecastUseCase(locationName, 3) } returnsMany
                    listOf(
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Failure(error))
                            },
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Success(forecast))
                            }
                    )

            val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

            viewModel.uiState.test {
                // ViewModel init runs synchronously and sets isLoading=true immediately
                val initialLoadingState = awaitItem()
                assertTrue(initialLoadingState.isLoading)

                // Advance scheduler to start flow collection
                advanceUntilIdle()

                // Flow emits Failure
                val errorState = awaitItem()
                assertEquals(error, errorState.error)
                assertFalse(errorState.isLoading)

                viewModel.retry()
                advanceUntilIdle()

                // Retry modifies state locally: isLoading=true, error=null
                val retryLoadingState = awaitItem()
                assertTrue(retryLoadingState.isLoading)
                assertNull(retryLoadingState.error)

                // Flow emits Success
                advanceUntilIdle()
                val successState = awaitItem()
                assertEquals(forecast, successState.forecast)
                assertFalse(successState.isLoading)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Refresh Functionality")
    inner class RefreshFunctionalityTest {

        @Test
        @DisplayName(
                "GIVEN existing forecast WHEN refresh is called THEN should reload and show isRefreshing"
        )
        fun `given existing forecast when refresh is called then reloads and shows isRefreshing`() =
                runTest {
                    // Given
                    val testDispatcher = StandardTestDispatcher(testScheduler)
                    Dispatchers.setMain(testDispatcher)
                    val locationName = "Bogotá"
                    val initialForecast = createForecast(locationName)
                    val updatedForecast = createForecast(locationName, "Sunny")
                    io.mockk.every { savedStateHandle.get<String>("locationName") } returns
                            locationName

                    coEvery { getForecastUseCase(locationName, 3) } returnsMany
                            listOf(
                                    flow {
                                        emit(ResultState.Loading())
                                        delay(1)
                                        emit(ResultState.Success(initialForecast))
                                    },
                                    flow {
                                        emit(ResultState.Loading(initialForecast))
                                        delay(1)
                                        emit(ResultState.Success(updatedForecast))
                                    }
                            )

                    val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

                    viewModel.uiState.test {
                        // ViewModel init runs synchronously and sets isLoading=true immediately
                        val initialLoadingState = awaitItem()
                        assertTrue(initialLoadingState.isLoading)

                        // Advance scheduler to start flow collection
                        advanceUntilIdle()

                        // Flow emits Success
                        val initialSuccessState = awaitItem()
                        assertEquals(initialForecast, initialSuccessState.forecast)
                        assertFalse(initialSuccessState.isLoading)

                        viewModel.refresh()
                        advanceUntilIdle()

                        // Local update: isRefreshing=true
                        val refreshingState = awaitItem()
                        assertTrue(refreshingState.isRefreshing)
                        assertFalse(refreshingState.isLoading)

                        // Flow emits Success
                        advanceUntilIdle()
                        val successState = awaitItem()
                        assertEquals(updatedForecast, successState.forecast)
                        assertFalse(successState.isRefreshing)

                        cancelAndIgnoreRemainingEvents()
                    }
                }

        @Test
        @DisplayName("GIVEN no existing forecast WHEN refresh is called THEN should show isLoading")
        fun `given no existing forecast when refresh is called then shows isLoading`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val locationName = "Bogotá"
            val forecast = createForecast(locationName)
            io.mockk.every { savedStateHandle.get<String>("locationName") } returns locationName

            coEvery { getForecastUseCase(locationName, 3) } returnsMany
                    listOf(
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Failure(AppError.NetworkError.NoConnection))
                            },
                            flow {
                                emit(ResultState.Loading())
                                delay(1)
                                emit(ResultState.Success(forecast))
                            }
                    )

            val viewModel = ForecastViewModel(getForecastUseCase, savedStateHandle)

            viewModel.uiState.test {
                // ViewModel init runs synchronously and sets isLoading=true immediately
                val initialLoadingState = awaitItem()
                assertTrue(initialLoadingState.isLoading)

                // Advance scheduler to start flow collection
                advanceUntilIdle()

                // Flow emits Failure
                val errorState = awaitItem()
                assertNotNull(errorState.error)
                assertFalse(errorState.isLoading)

                viewModel.refresh()
                advanceUntilIdle()

                // Local update: isLoading=true (since forecast is null)
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                assertFalse(loadingState.isRefreshing)

                // Flow emits Success
                advanceUntilIdle()
                val successState = awaitItem()
                assertEquals(forecast, successState.forecast)
                assertFalse(successState.isLoading)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // Helper functions
    private fun createForecast(
            locationName: String,
            condition: String = "Partly cloudy"
    ): Forecast =
            Forecast(
                    locationName = locationName,
                    days =
                            listOf(
                                    ForecastDay(
                                            date = "2024-01-15",
                                            avgTempC = 18.5,
                                            conditionText = condition,
                                            conditionIconUrl =
                                                    "https://cdn.weatherapi.com/weather/64x64/day/116.png"
                                    ),
                                    ForecastDay(
                                            date = "2024-01-16",
                                            avgTempC = 20.0,
                                            conditionText = "Sunny",
                                            conditionIconUrl =
                                                    "https://cdn.weatherapi.com/weather/64x64/day/113.png"
                                    ),
                                    ForecastDay(
                                            date = "2024-01-17",
                                            avgTempC = 16.8,
                                            conditionText = "Light rain",
                                            conditionIconUrl =
                                                    "https://cdn.weatherapi.com/weather/64x64/day/296.png"
                                    )
                            )
            )
}
