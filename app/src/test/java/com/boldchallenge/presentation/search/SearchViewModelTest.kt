package com.boldchallenge.presentation.search

import app.cash.turbine.test
import com.domain.error.AppError
import com.domain.model.Location
import com.domain.result.ResultState
import com.domain.usecase.SearchLocationsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SearchViewModel Tests")
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var searchLocationsUseCase: SearchLocationsUseCase

    private lateinit var viewModel: SearchViewModel
    
    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(searchLocationsUseCase)
    }
    
    @BeforeEach
    fun setup() {
        searchLocationsUseCase = mockk(relaxed = true)
        // Create ViewModel in setup for tests that don't need debounce
        // Tests that need debounce should create ViewModel inside runTest
        viewModel = SearchViewModel(searchLocationsUseCase)
    }

    @Nested
    @DisplayName("Query Changes & Debounce")
    inner class QueryChangesAndDebounceTest {

        @Test
        @DisplayName("GIVEN initial state WHEN ViewModel is created THEN should have empty query")
        fun `given initial state when ViewModel is created then has empty query`() = runTest {
            // Given
            val viewModel = createViewModel()

            // When
            val initialState = viewModel.uiState.value

            // Then
            assertEquals("", initialState.query)
            assertFalse(initialState.isLoading)
            assertTrue(initialState.locations.isEmpty())
            assertNull(initialState.error)
        }

        @Test
        @DisplayName("GIVEN query change WHEN onQueryChanged is called THEN should update query immediately")
        fun `given query change when onQueryChanged is called then updates query immediately`() = runTest {
            // Given
            val viewModel = createViewModel()
            val query = "Bogotá"

            // When
            viewModel.onQueryChanged(query)

            // Then
            val state = viewModel.uiState.value
            assertEquals(query, state.query)
        }

        @Test
        @DisplayName("GIVEN query change WHEN debounce delay passes THEN should trigger search")
        fun `given query change when debounce delay passes then triggers search`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val viewModel = createViewModel()
            // Give the ViewModel's init block time to start the flow collection
            advanceUntilIdle()
            
            val query = "Bogotá"
            val locations = createLocations()
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Success(locations)
            )

            // When
            viewModel.onQueryChanged(query)
            // Advance time to trigger debounce (500ms) plus a bit more to ensure processing
            advanceTimeBy(600)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            coVerify(exactly = 1) { searchLocationsUseCase(query) }
            
            Dispatchers.resetMain()
        }

        @Test
        @DisplayName("GIVEN multiple query changes WHEN debounce delay not passed THEN should not trigger search")
        fun `given multiple query changes when debounce delay not passed then does not trigger search`() = runTest {
            // Given
            coEvery { searchLocationsUseCase(any()) } returns flowOf(
                ResultState.Success(emptyList())
            )

            // When
            viewModel.onQueryChanged("B")
            advanceTimeBy(200)
            viewModel.onQueryChanged("Bo")
            advanceTimeBy(200)
            viewModel.onQueryChanged("Bog")
            advanceTimeBy(200)
            // Don't wait for full debounce delay

            // Then
            coVerify(exactly = 0) { searchLocationsUseCase(any()) }
        }

        @Test
        @DisplayName("GIVEN query change WHEN previous search is in progress THEN should cancel previous search")
        fun `given query change when previous search is in progress then cancels previous search`() = runTest {
            // Given
            val firstQuery = "Bogotá"
            val secondQuery = "Medellín"
            val locations = createLocations()

            coEvery { searchLocationsUseCase(firstQuery) } returns flowOf(
                ResultState.Loading(),
                ResultState.Success(locations)
            )
            coEvery { searchLocationsUseCase(secondQuery) } returns flowOf(
                ResultState.Success(locations)
            )

            // When
            viewModel.onQueryChanged(firstQuery)
            advanceTimeBy(500)
            viewModel.onQueryChanged(secondQuery)
            advanceTimeBy(500)

            // Then
            val state = viewModel.uiState.value
            assertEquals(secondQuery, state.query)
        }
    }

    @Nested
    @DisplayName("Query Validation")
    inner class QueryValidationTest {

        @Test
        @DisplayName("GIVEN query with less than 2 characters WHEN onQueryChanged is called THEN should not execute search")
        fun `given query with less than 2 characters when onQueryChanged is called then does not execute search`() = runTest {
            // Given
            val query = "B"

            // When
            viewModel.onQueryChanged(query)
            advanceTimeBy(500)

            // Then
            coVerify(exactly = 0) { searchLocationsUseCase(any()) }
            val state = viewModel.uiState.value
            assertTrue(state.locations.isEmpty())
            assertFalse(state.isLoading)
        }

        @Test
        @DisplayName("GIVEN query with exactly 2 characters WHEN debounce passes THEN should execute search")
        fun `given query with exactly 2 characters when debounce passes then executes search`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val viewModel = createViewModel()
            // Give the ViewModel's init block time to start the flow collection
            advanceUntilIdle()
            
            val query = "Bo"
            val locations = createLocations()
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Success(locations)
            )

            // When
            viewModel.onQueryChanged(query)
            // Advance time to trigger debounce (500ms) plus a bit more to ensure processing
            advanceTimeBy(600)
            advanceUntilIdle() // Wait for all coroutines to complete

            // Then
            coVerify(exactly = 1) { searchLocationsUseCase(query) }
            
            Dispatchers.resetMain()
        }

        @Test
        @DisplayName("GIVEN empty query WHEN onQueryChanged is called THEN should clear results")
        fun `given empty query when onQueryChanged is called then clears results`() = runTest {
            // Given
            val initialLocations = createLocations()
            viewModel.onQueryChanged("Bogotá")
            advanceTimeBy(500)

            // When
            viewModel.onQueryChanged("")

            // Then
            val state = viewModel.uiState.value
            assertEquals("", state.query)
            assertTrue(state.locations.isEmpty())
            assertFalse(state.isLoading)
        }
    }

    @Nested
    @DisplayName("Loading States")
    inner class LoadingStatesTest {

        @Test
        @DisplayName("GIVEN valid query WHEN search starts THEN should set isLoading to true")
        fun `given valid query when search starts then sets isLoading to true`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val viewModel = createViewModel()
            advanceUntilIdle() // Give the ViewModel's init block time to start the flow collection
            
            val query = "Bogotá"
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Loading(),
                ResultState.Success(emptyList())
            )

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(600) // Advance time to trigger debounce
                advanceUntilIdle()
                
                // Wait for loading state
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                cancelAndIgnoreRemainingEvents()
            }
            
            Dispatchers.resetMain()
        }

        @Test
        @DisplayName("GIVEN loading with cached data WHEN ResultState.Loading has data THEN should show cached data")
        fun `given loading with cached data when ResultState Loading has data then shows cached data`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val viewModel = createViewModel()
            advanceUntilIdle() // Give the ViewModel's init block time to start the flow collection
            
            val query = "Bogotá"
            val cachedLocations = createLocations()
            coEvery { searchLocationsUseCase(query) } returns flow {
                emit(ResultState.Loading(cachedLocations))
                kotlinx.coroutines.delay(1)
                emit(ResultState.Success(cachedLocations))
            }

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(600) // Advance time to trigger debounce
                advanceUntilIdle()
                
                // Wait for initial loading state (isLoading = true, no locations yet)
                val initialLoadingState = awaitItem()
                assertTrue(initialLoadingState.isLoading)
                
                // Wait for loading state with cached data (ResultState.Loading with cached data)
                advanceUntilIdle() // Advance time to process the delay(1) in the flow
                val state = awaitItem()
                assertTrue(state.isLoading)
                assertEquals(cachedLocations, state.locations)
                cancelAndIgnoreRemainingEvents()
            }
            
            Dispatchers.resetMain()
        }
    }

    @Nested
    @DisplayName("Success States")
    inner class SuccessStatesTest {

        @Test
        @DisplayName("GIVEN successful search WHEN ResultState.Success is received THEN should update locations and set isLoading to false")
        fun `given successful search when ResultState Success is received then updates locations and sets isLoading to false`() = runTest {
            // Given
            val query = "Bogotá"
            val locations = createLocations()
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Loading(),
                ResultState.Success(locations)
            )

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(500)
                
                // Wait for loading state
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                
                // Wait for success state
                val successState = awaitItem()
                assertFalse(successState.isLoading)
                assertEquals(locations, successState.locations)
                assertNull(successState.error)
                assertFalse(successState.isEmpty)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("GIVEN empty results WHEN search completes successfully THEN should set isEmpty to true")
        fun `given empty results when search completes successfully then sets isEmpty to true`() = runTest {
            // Given
            val query = "xyz123"
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Success(emptyList())
            )

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(500)
                
                // Wait for loading state
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                
                // Wait for success state with empty results
                val emptyState = awaitItem()
                assertFalse(emptyState.isLoading)
                assertTrue(emptyState.locations.isEmpty())
                assertTrue(emptyState.isEmpty)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTest {

        @Test
        @DisplayName("GIVEN search failure WHEN ResultState.Failure is received THEN should update error state")
        fun `given search failure when ResultState Failure is received then updates error state`() = runTest {
            // Given
            val query = "Bogotá"
            val error = AppError.NetworkError.NoConnection
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Loading(),
                ResultState.Failure(error)
            )

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(500)
                
                // Wait for loading state
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                
                // Wait for error state
                val errorState = awaitItem()
                assertFalse(errorState.isLoading)
                assertEquals(error, errorState.error)
                assertTrue(errorState.showError)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("GIVEN error state WHEN query is changed THEN should clear error")
        fun `given error state when query is changed then clears error`() = runTest {
            // Given
            val query = "Bogotá"
            val error = AppError.NetworkError.NoConnection
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Failure(error)
            )
            viewModel.onQueryChanged(query)
            advanceTimeBy(500)

            // When
            viewModel.onQueryChanged("New")

            // Then
            val state = viewModel.uiState.value
            assertNull(state.error)
        }

        @Test
        @DisplayName("GIVEN error with cached data WHEN ResultState.Failure is received THEN should not show error")
        fun `given error with cached data when ResultState Failure is received then does not show error`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val viewModel = createViewModel()
            advanceUntilIdle() // Give the ViewModel's init block time to start the flow collection
            
            val query = "Bogotá"
            val cachedLocations = createLocations()
            val error = AppError.NetworkError.NoConnection
            coEvery { searchLocationsUseCase(query) } returns flow {
                emit(ResultState.Loading(cachedLocations))
                kotlinx.coroutines.delay(1)
                emit(ResultState.Failure(error))
            }

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(600) // Advance time to trigger debounce
                advanceUntilIdle()
                
                // Wait for initial loading state (isLoading = true, no locations yet)
                val initialLoadingState = awaitItem()
                assertTrue(initialLoadingState.isLoading)
                
                // Wait for loading with cache (ResultState.Loading with cached data)
                advanceUntilIdle() // Advance time to process the delay(1) in the flow
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                assertEquals(cachedLocations, loadingState.locations)
                
                // Wait for error state with cached data
                advanceUntilIdle() // Advance time to process the delay(1) in the flow
                val state = awaitItem()
                assertFalse(state.isLoading)
                assertFalse(state.showError) // Should not show error when cached data exists
                assertEquals(cachedLocations, state.locations) // Cached locations should be preserved
                assertNotNull(state.error) // Error should still be set
                cancelAndIgnoreRemainingEvents()
            }
            
            Dispatchers.resetMain()
        }

        @Test
        @DisplayName("GIVEN exception in flow WHEN search fails THEN should map to UnknownError")
        fun `given exception in flow when search fails then maps to UnknownError`() = runTest {
            // Given
            val query = "Bogotá"
            val exception = RuntimeException("Network error")
            coEvery { searchLocationsUseCase(query) } returns flow {
                emit(ResultState.Loading())
                throw exception
            }

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(500)
                advanceUntilIdle()
                
                // Wait for loading state
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)
                
                // Wait for error state (exception caught by .catch in performSearch)
                advanceUntilIdle()
                val errorState = awaitItem()
                assertTrue(errorState.error is AppError.UnknownError)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Retry Functionality")
    inner class RetryFunctionalityTest {

        @Test
        @DisplayName("GIVEN error state WHEN retry is called with valid query THEN should execute search again")
        fun `given error state when retry is called with valid query then executes search again`() = runTest {
            // Given
            val testDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val viewModel = createViewModel()
            advanceUntilIdle() // Give the ViewModel's init block time to start the flow collection
            
            val query = "Bogotá"
            val error = AppError.NetworkError.NoConnection
            val locations = createLocations()

            coEvery { searchLocationsUseCase(query) } returnsMany listOf(
                flowOf(ResultState.Failure(error)),
                flowOf(ResultState.Success(locations))
            )

            // When & Then
            viewModel.uiState.test {
                // Skip initial state
                awaitItem()
                
                viewModel.onQueryChanged(query)
                // Wait for query update
                val queryUpdate = awaitItem()
                assertEquals(query, queryUpdate.query)
                
                advanceTimeBy(600) // Advance time to trigger debounce
                advanceUntilIdle()
                
                // Wait for initial loading state (isLoading = true, no error yet)
                val initialLoadingState = awaitItem()
                assertTrue(initialLoadingState.isLoading)
                
                // Wait for error state (ResultState.Failure)
                advanceUntilIdle()
                val errorState = awaitItem()
                assertFalse(errorState.isLoading)
                assertEquals(error, errorState.error)
                
                // Retry
                viewModel.retry()
                advanceUntilIdle()
                
                // Wait for loading state after retry
                val retryLoadingState = awaitItem()
                assertTrue(retryLoadingState.isLoading)
                
                // Wait for success state after retry
                advanceUntilIdle()
                val successState = awaitItem()
                assertEquals(locations, successState.locations)
                assertNull(successState.error)
                
                coVerify(exactly = 2) { searchLocationsUseCase(query) }
                cancelAndIgnoreRemainingEvents()
            }
            
            Dispatchers.resetMain()
        }

        @Test
        @DisplayName("GIVEN empty query WHEN retry is called THEN should not execute search")
        fun `given empty query when retry is called then does not execute search`() = runTest {
            // Given
            viewModel.onQueryChanged("")

            // When
            viewModel.retry()

            // Then
            coVerify(exactly = 0) { searchLocationsUseCase(any()) }
        }
    }

    @Nested
    @DisplayName("Clear Error")
    inner class ClearErrorTest {

        @Test
        @DisplayName("GIVEN error state WHEN clearError is called THEN should remove error")
        fun `given error state when clearError is called then removes error`() = runTest {
            // Given
            val query = "Bogotá"
            val error = AppError.NetworkError.NoConnection
            coEvery { searchLocationsUseCase(query) } returns flowOf(
                ResultState.Failure(error)
            )
            viewModel.onQueryChanged(query)
            advanceTimeBy(500)

            // When
            viewModel.clearError()

            // Then
            val state = viewModel.uiState.value
            assertNull(state.error)
        }
    }

    // Helper functions
    private fun createLocations(): List<Location> = listOf(
        Location(
            id = 1,
            name = "Bogotá",
            region = "Cundinamarca",
            country = "Colombia",
            lat = 4.7110,
            lon = -74.0721,
            url = null
        ),
        Location(
            id = 2,
            name = "Medellín",
            region = "Antioquia",
            country = "Colombia",
            lat = 6.2442,
            lon = -75.5812,
            url = null
        )
    )
}

