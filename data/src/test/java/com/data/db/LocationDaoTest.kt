package com.data.db

import app.cash.turbine.test
import com.data.db.DatabaseTestHelper.closeDatabase
import com.data.db.DatabaseTestHelper.createInMemoryDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("LocationDao Tests")
class LocationDaoTest {

    private lateinit var database: LocationDatabase
    private lateinit var locationDao: LocationDao

    @BeforeEach
    fun setup() {
        database = createInMemoryDatabase()
        locationDao = database.locationDao()
    }

    @AfterEach
    fun tearDown() {
        closeDatabase(database)
    }

    @Nested
    @DisplayName("Insert Operations")
    inner class InsertOperationsTest {

        @Test
        @DisplayName("GIVEN a list of locations WHEN inserting all THEN should save all locations")
        fun `given list of locations when inserting all then saves all locations`() = runTest {
            // Given
            val locations = listOf(
                createLocationEntity(1L, "Bogotá", "bog"),
                createLocationEntity(2L, "Medellín", "med")
            )

            // When
            locationDao.insertAll(locations)

            // Then
            val allLocations = locationDao.searchByQuery("bog").first()
            assertEquals(1, allLocations.size)
            assertEquals("Bogotá", allLocations[0].name)
        }

        @Test
        @DisplayName("GIVEN duplicate locations WHEN inserting with REPLACE strategy THEN should replace existing")
        fun `given duplicate locations when inserting with REPLACE strategy then replaces existing`() =
            runTest {
                // Given
                val originalLocation = createLocationEntity(1L, "Bogotá", "bog")
                locationDao.insertAll(listOf(originalLocation))

                val updatedLocation = originalLocation.copy(name = "Bogotá D.C.")

                // When
                locationDao.insertAll(listOf(updatedLocation))

                // Then
                val result = locationDao.searchByQuery("bog").first()
                assertEquals(1, result.size)
                assertEquals("Bogotá D.C.", result[0].name)
                assertEquals(originalLocation.id, result[0].id)
            }

        @Test
        @DisplayName("GIVEN empty list WHEN inserting all THEN should not throw exception")
        fun `given empty list when inserting all then does not throw exception`() = runTest {
            // When & Then - should not throw
            locationDao.insertAll(emptyList())

            val result = locationDao.searchByQuery("").first()
            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("Search Operations")
    inner class SearchOperationsTest {

        @Test
        @DisplayName("GIVEN locations with matching query prefix WHEN searching THEN should return matching locations")
        fun `given locations with matching query prefix when searching then returns matching locations`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "Bogotá", "bog"),
                        createLocationEntity(2L, "Bogotá D.C.", "bog"),
                        createLocationEntity(3L, "Medellín", "med")
                    )
                )

                // When
                val result = locationDao.searchByQuery("bog").first()

                // Then
                assertEquals(2, result.size)
                assertTrue(result.any { it.name == "Bogotá" })
                assertTrue(result.any { it.name == "Bogotá D.C." })
            }

        @Test
        @DisplayName("GIVEN locations with partial query match WHEN searching THEN should return matching locations using LIKE pattern")
        fun `given locations with partial query match when searching then returns matching locations using LIKE pattern`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "London", "lon"),
                        createLocationEntity(2L, "Los Angeles", "los"),
                        createLocationEntity(3L, "Los Santos", "los"),
                        createLocationEntity(4L, "Paris", "par")
                    )
                )

                // When - LIKE pattern: "lo" should match "lon" and "los"
                val result = locationDao.searchByQuery("lo").first()

                // Then
                assertEquals(3, result.size)
                assertTrue(result.any { it.name == "London" })
                assertTrue(result.any { it.name == "Los Angeles" })
                assertTrue(result.any { it.name == "Los Santos" })
            }

        @Test
        @DisplayName("GIVEN no matching locations WHEN searching THEN should return empty list")
        fun `given no matching locations when searching then returns empty list`() = runTest {
            // Given
            locationDao.insertAll(
                listOf(
                    createLocationEntity(1L, "Bogotá", "bog")
                )
            )

            // When
            val result = locationDao.searchByQuery("med").first()

            // Then
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("GIVEN empty database WHEN searching THEN should return empty list")
        fun `given empty database when searching then returns empty list`() = runTest {
            // When
            val result = locationDao.searchByQuery("any").first()

            // Then
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("GIVEN locations WHEN searching with empty query THEN should return locations with empty query")
        fun `given locations when searching with empty query then returns locations with empty query`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "Bogotá", ""),
                        createLocationEntity(2L, "Medellín", "med")
                    )
                )

                // When
                val result = locationDao.searchByQuery("").first()

                // Then
                assertEquals(1, result.size)
                assertEquals("Bogotá", result[0].name)
            }

        @Test
        @DisplayName("GIVEN multiple locations WHEN searching THEN should observe Flow emissions correctly")
        fun `given multiple locations when searching then observes Flow emissions correctly`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "Bogotá", "bog"),
                        createLocationEntity(2L, "Bogotá D.C.", "bog")
                    )
                )

                // When
                locationDao.searchByQuery("bog").test {
                    // Then
                    val items = awaitItem()
                    assertEquals(2, items.size)
                    awaitComplete()
                }
            }
    }

    @Nested
    @DisplayName("Clear Operations")
    inner class ClearOperationsTest {

        @Test
        @DisplayName("GIVEN locations with same query WHEN clearing by query THEN should delete matching locations")
        fun `given locations with same query when clearing by query then deletes matching locations`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "Bogotá", "bog"),
                        createLocationEntity(2L, "Bogotá D.C.", "bog"),
                        createLocationEntity(3L, "Medellín", "med")
                    )
                )

                // When
                locationDao.clearByQuery("bog")

                // Then
                val bogotaResults = locationDao.searchByQuery("bog").first()
                assertTrue(bogotaResults.isEmpty())

                val medellinResults = locationDao.searchByQuery("med").first()
                assertEquals(1, medellinResults.size)
                assertEquals("Medellín", medellinResults[0].name)
            }

        @Test
        @DisplayName("GIVEN no matching locations WHEN clearing by query THEN should not throw exception")
        fun `given no matching locations when clearing by query then does not throw exception`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "Bogotá", "bog")
                    )
                )

                // When & Then - should not throw
                locationDao.clearByQuery("med")

                val result = locationDao.searchByQuery("bog").first()
                assertEquals(1, result.size)
            }

        @Test
        @DisplayName("GIVEN empty database WHEN clearing by query THEN should not throw exception")
        fun `given empty database when clearing by query then does not throw exception`() =
            runTest {
                // When & Then - should not throw
                locationDao.clearByQuery("any")
            }

        @Test
        @DisplayName("GIVEN locations WHEN clearing and reinserting THEN should work correctly")
        fun `given locations when clearing and reinserting then works correctly`() = runTest {
            // Given
            val locations1 = listOf(
                createLocationEntity(1L, "Bogotá", "bog"),
                createLocationEntity(2L, "Bogotá D.C.", "bog")
            )
            locationDao.insertAll(locations1)

            // When
            locationDao.clearByQuery("bog")
            val newLocations = listOf(createLocationEntity(3L, "Bogotá Nueva", "bog"))
            locationDao.insertAll(newLocations)

            // Then
            val result = locationDao.searchByQuery("bog").first()
            assertEquals(1, result.size)
            assertEquals("Bogotá Nueva", result[0].name)
            assertEquals(3L, result[0].id)
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    inner class IntegrationScenariosTest {

        @Test
        @DisplayName("GIVEN complete workflow WHEN performing all operations THEN should work correctly")
        fun `given complete workflow when performing all operations then works correctly`() =
            runTest {
                // Given & When
                val initialLocations = listOf(
                    createLocationEntity(1L, "Bogotá", "bog"),
                    createLocationEntity(2L, "Medellín", "med")
                )
                locationDao.insertAll(initialLocations)

                // Verify insert
                val initialResults = locationDao.searchByQuery("bog").first()
                assertEquals(1, initialResults.size)

                // Clear one query
                locationDao.clearByQuery("bog")
                val afterClear = locationDao.searchByQuery("bog").first()
                assertTrue(afterClear.isEmpty())

                // Insert again
                locationDao.insertAll(listOf(createLocationEntity(3L, "Bogotá New", "bog")))
                val finalResults = locationDao.searchByQuery("bog").first()
                assertEquals(1, finalResults.size)
                assertEquals("Bogotá New", finalResults[0].name)
            }

        @Test
        @DisplayName("GIVEN multiple queries WHEN searching different queries THEN should return correct results")
        fun `given multiple queries when searching different queries then returns correct results`() =
            runTest {
                // Given
                locationDao.insertAll(
                    listOf(
                        createLocationEntity(1L, "Bogotá", "bog"),
                        createLocationEntity(2L, "Bogotá D.C.", "bog"),
                        createLocationEntity(3L, "Medellín", "med"),
                        createLocationEntity(4L, "Cali", "cal")
                    )
                )

                // When & Then
                val bogotaResults = locationDao.searchByQuery("bog").first()
                assertEquals(2, bogotaResults.size)

                val medellinResults = locationDao.searchByQuery("med").first()
                assertEquals(1, medellinResults.size)
                assertEquals("Medellín", medellinResults[0].name)

                val caliResults = locationDao.searchByQuery("cal").first()
                assertEquals(1, caliResults.size)
                assertEquals("Cali", caliResults[0].name)
            }
    }

    // Helper methods
    private fun createLocationEntity(
        id: Long,
        name: String,
        query: String
    ): LocationEntity {
        return LocationEntity(
            id = id,
            name = name,
            region = "Region",
            country = "Country",
            lat = 4.6097,
            lon = -74.0817,
            url = "$name-url",
            query = query
        )
    }
}
