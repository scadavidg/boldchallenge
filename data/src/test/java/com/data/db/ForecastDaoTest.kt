package com.data.db

import app.cash.turbine.test
import com.data.db.DatabaseTestHelper.closeDatabase
import com.data.db.DatabaseTestHelper.createInMemoryDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ForecastDao Tests")
class ForecastDaoTest {

    private lateinit var database: LocationDatabase
    private lateinit var forecastDao: ForecastDao

    @BeforeEach
    fun setup() {
        database = createInMemoryDatabase()
        forecastDao = database.forecastDao()
    }

    @AfterEach
    fun tearDown() {
        closeDatabase(database)
    }

    @Nested
    @DisplayName("Insert/Upsert Operations")
    inner class InsertUpsertOperationsTest {

        @Test
        @DisplayName("GIVEN a forecast entity WHEN upserting THEN should save the forecast")
        fun `given forecast entity when upserting then saves the forecast`() = runTest {
            // Given
            val forecast = createForecastEntity("Bogotá", "{\"test\": \"data\"}")

            // When
            forecastDao.upsertForecast(forecast)

            // Then
            val result = forecastDao.getForecast("Bogotá").first()
            assertTrue(result != null)
            assertEquals("Bogotá", result?.locationName)
            assertEquals("{\"test\": \"data\"}", result?.serializedForecast)
        }

        @Test
        @DisplayName("GIVEN existing forecast WHEN upserting with same locationName THEN should replace existing")
        fun `given existing forecast when upserting with same locationName then replaces existing`() =
            runTest {
                // Given
                val originalForecast = createForecastEntity("Bogotá", "{\"old\": \"data\"}")
                forecastDao.upsertForecast(originalForecast)

                val updatedForecast = originalForecast.copy(
                    serializedForecast = "{\"new\": \"data\"}",
                    lastUpdated = System.currentTimeMillis()
                )

                // When
                forecastDao.upsertForecast(updatedForecast)

                // Then
                val result = forecastDao.getForecast("Bogotá").first()
                assertTrue(result != null)
                assertEquals("Bogotá", result?.locationName)
                assertEquals("{\"new\": \"data\"}", result?.serializedForecast)
                assertTrue(result?.lastUpdated!! > originalForecast.lastUpdated)
            }

        @Test
        @DisplayName("GIVEN multiple forecasts WHEN upserting each THEN should save all forecasts")
        fun `given multiple forecasts when upserting each then saves all forecasts`() = runTest {
            // Given
            val forecast1 = createForecastEntity("Bogotá", "{\"data1\": \"value1\"}")
            val forecast2 = createForecastEntity("Medellín", "{\"data2\": \"value2\"}")
            val forecast3 = createForecastEntity("Cali", "{\"data3\": \"value3\"}")

            // When
            forecastDao.upsertForecast(forecast1)
            forecastDao.upsertForecast(forecast2)
            forecastDao.upsertForecast(forecast3)

            // Then
            val result1 = forecastDao.getForecast("Bogotá").first()
            assertTrue(result1 != null)
            assertEquals("Bogotá", result1?.locationName)

            val result2 = forecastDao.getForecast("Medellín").first()
            assertTrue(result2 != null)
            assertEquals("Medellín", result2?.locationName)

            val result3 = forecastDao.getForecast("Cali").first()
            assertTrue(result3 != null)
            assertEquals("Cali", result3?.locationName)
        }
    }

    @Nested
    @DisplayName("Get Operations")
    inner class GetOperationsTest {

        @Test
        @DisplayName("GIVEN existing forecast WHEN getting by locationName THEN should return the forecast")
        fun `given existing forecast when getting by locationName then returns the forecast`() =
            runTest {
                // Given
                val forecast = createForecastEntity("Bogotá", "{\"test\": \"data\"}")
                forecastDao.upsertForecast(forecast)

                // When
                val result = forecastDao.getForecast("Bogotá").first()

                // Then
                assertTrue(result != null)
                assertEquals("Bogotá", result?.locationName)
                assertEquals("{\"test\": \"data\"}", result?.serializedForecast)
            }

        @Test
        @DisplayName("GIVEN no forecast WHEN getting by locationName THEN should return null")
        fun `given no forecast when getting by locationName then returns null`() = runTest {
            // When
            val result = forecastDao.getForecast("NonExistent").first()

            // Then
            assertNull(result)
        }

        @Test
        @DisplayName("GIVEN forecast with special characters in locationName WHEN getting THEN should return correctly")
        fun `given forecast with special characters in locationName when getting then returns correctly`() =
            runTest {
                // Given
                val locationName = "São Paulo"
                val forecast = createForecastEntity(locationName, "{\"test\": \"data\"}")
                forecastDao.upsertForecast(forecast)

                // When
                val result = forecastDao.getForecast(locationName).first()

                // Then
                assertTrue(result != null)
                assertEquals(locationName, result?.locationName)
            }

        @Test
        @DisplayName("GIVEN forecast WHEN getting THEN should observe Flow emissions correctly")
        fun `given forecast when getting then observes Flow emissions correctly`() = runTest {
            // Given
            val forecast = createForecastEntity("Bogotá", "{\"test\": \"data\"}")
            forecastDao.upsertForecast(forecast)

            // When
            forecastDao.getForecast("Bogotá").test {
                // Then
                val item = awaitItem()
                assertTrue(item != null)
                assertEquals("Bogotá", item?.locationName)
                awaitComplete()
            }
        }

        @Test
        @DisplayName("GIVEN empty database WHEN getting forecast THEN should return null in Flow")
        fun `given empty database when getting forecast then returns null in Flow`() = runTest {
            // When
            forecastDao.getForecast("AnyLocation").test {
                // Then
                val item = awaitItem()
                assertNull(item)
                awaitComplete()
            }
        }
    }

    @Nested
    @DisplayName("Update Scenarios")
    inner class UpdateScenariosTest {

        @Test
        @DisplayName("GIVEN existing forecast WHEN updating lastUpdated THEN should preserve other fields")
        fun `given existing forecast when updating lastUpdated then preserves other fields`() =
            runTest {
                // Given
                val originalForecast = createForecastEntity(
                    "Bogotá",
                    "{\"forecast\": \"data\"}",
                    lastUpdated = 1000L
                )
                forecastDao.upsertForecast(originalForecast)

                val updatedForecast = originalForecast.copy(lastUpdated = 2000L)

                // When
                forecastDao.upsertForecast(updatedForecast)

                // Then
                val result = forecastDao.getForecast("Bogotá").first()
                assertTrue(result != null)
                assertEquals("Bogotá", result?.locationName)
                assertEquals("{\"forecast\": \"data\"}", result?.serializedForecast)
                assertEquals(2000L, result?.lastUpdated)
            }

        @Test
        @DisplayName("GIVEN existing forecast WHEN updating serializedForecast THEN should update the data")
        fun `given existing forecast when updating serializedForecast then updates the data`() =
            runTest {
                // Given
                val originalForecast = createForecastEntity("Bogotá", "{\"old\": \"data\"}")
                forecastDao.upsertForecast(originalForecast)

                val updatedForecast = originalForecast.copy(
                    serializedForecast = "{\"new\": \"updated data\"}"
                )

                // When
                forecastDao.upsertForecast(updatedForecast)

                // Then
                val result = forecastDao.getForecast("Bogotá").first()
                assertTrue(result != null)
                assertEquals("{\"new\": \"updated data\"}", result?.serializedForecast)
            }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    inner class IntegrationScenariosTest {

        @Test
        @DisplayName("GIVEN complete workflow WHEN performing all operations THEN should work correctly")
        fun `given complete workflow when performing all operations then works correctly`() =
            runTest {
                // Insert
                val forecast1 = createForecastEntity("Bogotá", "{\"initial\": \"data\"}")
                forecastDao.upsertForecast(forecast1)

                // Verify insert
                val inserted = forecastDao.getForecast("Bogotá").first()
                assertTrue(inserted != null)
                assertEquals("{\"initial\": \"data\"}", inserted?.serializedForecast)

                // Update
                val forecast2 = createForecastEntity("Bogotá", "{\"updated\": \"data\"}")
                forecastDao.upsertForecast(forecast2)

                // Verify update
                val updated = forecastDao.getForecast("Bogotá").first()
                assertTrue(updated != null)
                assertEquals("{\"updated\": \"data\"}", updated?.serializedForecast)
            }

        @Test
        @DisplayName("GIVEN multiple forecasts WHEN querying each THEN should return correct forecast")
        fun `given multiple forecasts when querying each then returns correct forecast`() =
            runTest {
                // Given
                val forecast1 = createForecastEntity("Bogotá", "{\"city\": \"Bogotá\"}")
                val forecast2 = createForecastEntity("Medellín", "{\"city\": \"Medellín\"}")
                val forecast3 = createForecastEntity("Cali", "{\"city\": \"Cali\"}")

                forecastDao.upsertForecast(forecast1)
                forecastDao.upsertForecast(forecast2)
                forecastDao.upsertForecast(forecast3)

                // When & Then
                val result1 = forecastDao.getForecast("Bogotá").first()
                assertTrue(result1 != null)
                assertEquals("Bogotá", result1?.locationName)
                assertTrue(result1?.serializedForecast?.contains("Bogotá") == true)

                val result2 = forecastDao.getForecast("Medellín").first()
                assertTrue(result2 != null)
                assertEquals("Medellín", result2?.locationName)
                assertTrue(result2?.serializedForecast?.contains("Medellín") == true)

                val result3 = forecastDao.getForecast("Cali").first()
                assertTrue(result3 != null)
                assertEquals("Cali", result3?.locationName)
                assertTrue(result3?.serializedForecast?.contains("Cali") == true)
            }

        @Test
        @DisplayName("GIVEN forecast with large JSON WHEN upserting and getting THEN should handle correctly")
        fun `given forecast with large JSON when upserting and getting then handles correctly`() =
            runTest {
                // Given
                val largeJson = buildString {
                    append("{\"forecast\": [")
                    repeat(100) { i ->
                        if (i > 0) append(",")
                        append("{\"day\": $i, \"temp\": ${20 + i}}")
                    }
                    append("]}")
                }
                val forecast = createForecastEntity("Bogotá", largeJson)

                // When
                forecastDao.upsertForecast(forecast)
                val result = forecastDao.getForecast("Bogotá").first()

                // Then
                assertTrue(result != null)
                assertEquals("Bogotá", result?.locationName)
                assertEquals(largeJson, result?.serializedForecast)
            }
    }

    // Helper methods
    private fun createForecastEntity(
        locationName: String,
        serializedForecast: String,
        lastUpdated: Long = System.currentTimeMillis()
    ): ForecastEntity {
        return ForecastEntity(
            locationName = locationName,
            serializedForecast = serializedForecast,
            lastUpdated = lastUpdated
        )
    }
}
