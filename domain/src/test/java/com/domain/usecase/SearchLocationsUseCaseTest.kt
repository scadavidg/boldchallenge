package com.domain.usecase

import app.cash.turbine.test
import com.domain.error.AppError
import com.domain.model.Location
import com.domain.repository.LocationRepository
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

@DisplayName("SearchLocationsUseCase Tests")
class SearchLocationsUseCaseTest {

    private lateinit var repository: LocationRepository
    private lateinit var useCase: SearchLocationsUseCase

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = SearchLocationsUseCase(repository)
    }

    @Nested
    @DisplayName("Query Validation")
    inner class QueryValidationTest {

        @Test
        @DisplayName("GIVEN query with less than 2 characters WHEN invoking use case THEN should return Success with empty list without calling repository")
        fun `given query with less than 2 characters when invoking use case then returns Success with empty list without calling repository`() =
            runTest {
                // Given
                val query = "b"

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    val success = result as ResultState.Success<List<Location>>
                    assertTrue(success.data.isEmpty())

                    awaitComplete()
                }

                coVerify(exactly = 0) { repository.searchLocations(any()) }
            }

        @Test
        @DisplayName("GIVEN empty query WHEN invoking use case THEN should return Success with empty list without calling repository")
        fun `given empty query when invoking use case then returns Success with empty list without calling repository`() =
            runTest {
                // Given
                val query = ""

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    val success = result as ResultState.Success<List<Location>>
                    assertTrue(success.data.isEmpty())

                    awaitComplete()
                }

                coVerify(exactly = 0) { repository.searchLocations(any()) }
            }

        @Test
        @DisplayName("GIVEN query with exactly 2 characters WHEN invoking use case THEN should call repository")
        fun `given query with exactly 2 characters when invoking use case then calls repository`() =
            runTest {
                // Given
                val query = "bo"
                val locations = createLocations()
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Success(locations)
                )

                // When
                useCase(query).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify(exactly = 1) { repository.searchLocations(query) }
            }

        @Test
        @DisplayName("GIVEN query with more than 2 characters WHEN invoking use case THEN should call repository")
        fun `given query with more than 2 characters when invoking use case then calls repository`() =
            runTest {
                // Given
                val query = "bogota"
                val locations = createLocations()
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Success(locations)
                )

                // When
                useCase(query).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify(exactly = 1) { repository.searchLocations(query) }
            }
    }

    @Nested
    @DisplayName("Repository Delegation")
    inner class RepositoryDelegationTest {

        @Test
        @DisplayName("GIVEN valid query WHEN invoking use case THEN should call repository with correct query")
        fun `given valid query when invoking use case then calls repository with correct query`() =
            runTest {
                // Given
                val query = "bogota"
                val locations = createLocations()
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Success(locations)
                )

                // When
                useCase(query).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then
                coVerify(exactly = 1) { repository.searchLocations(query) }
            }
    }

    @Nested
    @DisplayName("Success Scenarios")
    inner class SuccessScenariosTest {

        @Test
        @DisplayName("GIVEN repository returns Success WHEN invoking use case with valid query THEN should return Success with locations")
        fun `given repository returns Success when invoking use case with valid query then returns Success with locations`() =
            runTest {
                // Given
                val query = "bogota"
                val locations = createLocations()
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Success(locations)
                )

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    val success = result as ResultState.Success<List<Location>>
                    assertEquals(locations, success.data)
                    assertEquals(2, success.data.size)
                    assertEquals("Bogotá", success.data[0].name)
                    assertEquals("Bogotá D.C.", success.data[1].name)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN repository returns Success with empty list WHEN invoking use case THEN should return Success with empty list")
        fun `given repository returns Success with empty list when invoking use case then returns Success with empty list`() =
            runTest {
                // Given
                val query = "xyz"
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Success(emptyList())
                )

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    val success = result as ResultState.Success<List<Location>>
                    assertTrue(success.data.isEmpty())

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
                val query = "bogota"
                val cachedLocations = createLocations()
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Loading(cachedLocations)
                )

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Loading)
                    val loading = result as ResultState.Loading<List<Location>>
                    assertEquals(cachedLocations, loading.data)

                    awaitComplete()
                }
            }

        @Test
        @DisplayName("GIVEN repository returns Loading without data WHEN invoking use case THEN should return Loading with null data")
        fun `given repository returns Loading without data when invoking use case then returns Loading with null data`() =
            runTest {
                // Given
                val query = "bogota"
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Loading<List<Location>>(null)
                )

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Loading)
                    val loading = result as ResultState.Loading<List<Location>>
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
                val query = "bogota"
                val error = AppError.NetworkError.Timeout
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Failure(error)
                )

                // When
                useCase(query).test {
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
                val query = "bogota"
                val error = AppError.NetworkError.HttpError(404, "Not Found")
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Failure(error)
                )

                // When
                useCase(query).test {
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
        @DisplayName("GIVEN repository returns UnknownError WHEN invoking use case THEN should return Failure with UnknownError")
        fun `given repository returns UnknownError when invoking use case then returns Failure with UnknownError`() =
            runTest {
                // Given
                val query = "bogota"
                val error = AppError.UnknownError("Unexpected error")
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Failure(error)
                )

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Failure)
                    val failure = result as ResultState.Failure
                    assertTrue(failure.error is AppError.UnknownError)

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
                val query = "bogota"
                val cachedLocations = createLocations()
                val freshLocations = createLocations()

                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Loading(cachedLocations),
                    ResultState.Success(freshLocations)
                )

                // When
                useCase(query).test {
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
                val query = "bogota"
                val locations = createLocations()
                coEvery { repository.searchLocations(query) } returns flowOf(
                    ResultState.Success(locations)
                )

                // When
                useCase(query).test {
                    awaitItem() // Success
                    awaitComplete()
                }

                // Then - test completes successfully (flow was closed)
            }

        @Test
        @DisplayName("GIVEN query validation returns empty list WHEN invoking use case THEN should complete immediately")
        fun `given query validation returns empty list when invoking use case then completes immediately`() =
            runTest {
                // Given
                val query = "a"

                // When
                useCase(query).test {
                    // Then
                    val result = awaitItem()
                    assertTrue(result is ResultState.Success)
                    assertTrue((result as ResultState.Success<List<Location>>).data.isEmpty())

                    awaitComplete()
                }
            }
    }

    // Helper methods
    private fun createLocations(): List<Location> {
        return listOf(
            Location(
                id = 1L,
                name = "Bogotá",
                region = "Cundinamarca",
                country = "Colombia",
                lat = 4.6097,
                lon = -74.0817,
                url = "bogota-colombia"
            ),
            Location(
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
}

