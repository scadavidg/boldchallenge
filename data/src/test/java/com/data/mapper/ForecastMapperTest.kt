package com.data.mapper

import com.data.db.ForecastEntity
import com.data.dto.ConditionDto
import com.data.dto.DayDto
import com.data.dto.ForecastDayDto
import com.data.dto.ForecastDto
import com.data.dto.ForecastResponseDto
import com.data.dto.LocationDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("ForecastMapper Tests")
class ForecastMapperTest {

    private lateinit var moshi: Moshi

    @BeforeEach
    fun setup() {
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Nested
    @DisplayName("ForecastResponseDto.toDomain()")
    inner class ForecastResponseDtoToDomainTest {

        @Test
        @DisplayName("GIVEN a ForecastResponseDto with multiple days WHEN mapping to domain THEN should return Forecast with correct location name and all days")
        fun `given ForecastResponseDto with multiple days when mapping to domain then returns Forecast with correct location name and all days`() {
            // Given
            val locationName = "Bogotá"
            val forecastDayDto1 = createForecastDayDto(
                date = "2024-01-15",
                avgTempC = 15.5,
                conditionText = "Sunny",
                iconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png"
            )
            val forecastDayDto2 = createForecastDayDto(
                date = "2024-01-16",
                avgTempC = 18.2,
                conditionText = "Cloudy",
                iconUrl = "//cdn.weatherapi.com/weather/64x64/day/116.png"
            )
            val forecastResponseDto = ForecastResponseDto(
                location = LocationDto(
                    id = 1L,
                    name = locationName,
                    region = "Cundinamarca",
                    country = "Colombia",
                    lat = 4.6097,
                    lon = -74.0817,
                    url = "bogota"
                ),
                forecast = ForecastDto(
                    forecastday = listOf(forecastDayDto1, forecastDayDto2)
                )
            )

            // When
            val result = forecastResponseDto.toDomain()

            // Then
            assertEquals(locationName, result.locationName)
            assertEquals(2, result.days.size)
            assertEquals("2024-01-15", result.days[0].date)
            assertEquals(15.5, result.days[0].avgTempC)
            assertEquals("Sunny", result.days[0].conditionText)
            assertEquals(
                "https://cdn.weatherapi.com/weather/64x64/day/113.png",
                result.days[0].conditionIconUrl
            )
        }

        @Test
        @DisplayName("GIVEN a ForecastResponseDto with empty days list WHEN mapping to domain THEN should return Forecast with empty days list")
        fun `given ForecastResponseDto with empty days when mapping to domain then returns Forecast with empty days list`() {
            // Given
            val locationName = "Medellín"
            val forecastResponseDto = ForecastResponseDto(
                location = LocationDto(
                    id = 2L,
                    name = locationName,
                    region = "Antioquia",
                    country = "Colombia",
                    lat = 6.2476,
                    lon = -75.5658,
                    url = "medellin"
                ),
                forecast = ForecastDto(forecastday = emptyList())
            )

            // When
            val result = forecastResponseDto.toDomain()

            // Then
            assertEquals(locationName, result.locationName)
            assertEquals(0, result.days.size)
        }
    }

    @Nested
    @DisplayName("ForecastDayDto.toDomain()")
    inner class ForecastDayDtoToDomainTest {

        @Test
        @DisplayName("GIVEN a ForecastDayDto with relative icon URL starting with // WHEN mapping to domain THEN should convert to absolute URL with https")
        fun `given ForecastDayDto with relative icon URL when mapping to domain then converts to absolute URL with https`() {
            // Given
            val relativeIconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png"
            val forecastDayDto = createForecastDayDto(
                date = "2024-01-15",
                avgTempC = 20.0,
                conditionText = "Clear",
                iconUrl = relativeIconUrl
            )

            // When
            val result = forecastDayDto.toDomain()

            // Then
            assertEquals("2024-01-15", result.date)
            assertEquals(20.0, result.avgTempC)
            assertEquals("Clear", result.conditionText)
            assertEquals(
                "https://cdn.weatherapi.com/weather/64x64/day/113.png",
                result.conditionIconUrl
            )
        }

        @Test
        @DisplayName("GIVEN a ForecastDayDto with absolute icon URL WHEN mapping to domain THEN should keep the absolute URL unchanged")
        fun `given ForecastDayDto with absolute icon URL when mapping to domain then keeps absolute URL unchanged`() {
            // Given
            val absoluteIconUrl = "https://cdn.weatherapi.com/weather/64x64/day/116.png"
            val forecastDayDto = createForecastDayDto(
                date = "2024-01-16",
                avgTempC = 18.5,
                conditionText = "Partly Cloudy",
                iconUrl = absoluteIconUrl
            )

            // When
            val result = forecastDayDto.toDomain()

            // Then
            assertEquals("2024-01-16", result.date)
            assertEquals(18.5, result.avgTempC)
            assertEquals("Partly Cloudy", result.conditionText)
            assertEquals(absoluteIconUrl, result.conditionIconUrl)
        }

        @Test
        @DisplayName("GIVEN a ForecastDayDto with icon URL not starting with // WHEN mapping to domain THEN should keep the URL unchanged")
        fun `given ForecastDayDto with icon URL not starting with double slash when mapping to domain then keeps URL unchanged`() {
            // Given
            val customIconUrl = "/custom/path/icon.png"
            val forecastDayDto = createForecastDayDto(
                date = "2024-01-17",
                avgTempC = 16.0,
                conditionText = "Rainy",
                iconUrl = customIconUrl
            )

            // When
            val result = forecastDayDto.toDomain()

            // Then
            assertEquals("2024-01-17", result.date)
            assertEquals(16.0, result.avgTempC)
            assertEquals("Rainy", result.conditionText)
            assertEquals(customIconUrl, result.conditionIconUrl)
        }
    }

    @Nested
    @DisplayName("ForecastResponseDto.toEntity()")
    inner class ForecastResponseDtoToEntityTest {

        @Test
        @DisplayName("GIVEN a ForecastResponseDto and location name WHEN mapping to entity THEN should return ForecastEntity with serialized JSON and current timestamp")
        fun `given ForecastResponseDto and location name when mapping to entity then returns ForecastEntity with serialized JSON and current timestamp`() {
            // Given
            val locationName = "Cali"
            val beforeTimestamp = System.currentTimeMillis()
            val forecastResponseDto = createForecastResponseDto(locationName = "Cali")

            // When
            val result = forecastResponseDto.toEntity(moshi, locationName)
            val afterTimestamp = System.currentTimeMillis()

            // Then
            assertEquals(locationName, result.locationName)
            assertEquals(true, result.lastUpdated in beforeTimestamp..afterTimestamp)
            assertEquals(true, result.serializedForecast.isNotEmpty())

            // Verify that the serialized JSON can be deserialized back
            val adapter = moshi.adapter(ForecastResponseDto::class.java)
            val deserializedDto = adapter.fromJson(result.serializedForecast)
            assertEquals(forecastResponseDto.location.name, deserializedDto?.location?.name)
            assertEquals(
                forecastResponseDto.forecast.forecastday.size,
                deserializedDto?.forecast?.forecastday?.size
            )
        }

        @Test
        @DisplayName("GIVEN a ForecastResponseDto with complex nested structure WHEN mapping to entity THEN should serialize and preserve all nested data")
        fun `given ForecastResponseDto with complex nested structure when mapping to entity then serializes and preserves all nested data`() {
            // Given
            val locationName = "Barranquilla"
            val forecastDayDto = createForecastDayDto(
                date = "2024-01-20",
                avgTempC = 28.5,
                conditionText = "Hot",
                iconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png"
            )
            val forecastResponseDto = ForecastResponseDto(
                location = LocationDto(
                    id = 5L,
                    name = locationName,
                    region = "Atlántico",
                    country = "Colombia",
                    lat = 10.9639,
                    lon = -74.7964,
                    url = "barranquilla"
                ),
                forecast = ForecastDto(
                    forecastday = listOf(forecastDayDto)
                )
            )

            // When
            val result = forecastResponseDto.toEntity(moshi, locationName)

            // Then
            val adapter = moshi.adapter(ForecastResponseDto::class.java)
            val deserializedDto = adapter.fromJson(result.serializedForecast)

            assertEquals(forecastResponseDto.location.name, deserializedDto?.location?.name)
            assertEquals(forecastResponseDto.location.id, deserializedDto?.location?.id)
            assertEquals(
                forecastResponseDto.forecast.forecastday.first().date,
                deserializedDto?.forecast?.forecastday?.first()?.date
            )
            assertEquals(
                forecastResponseDto.forecast.forecastday.first().day.avgTempC,
                deserializedDto?.forecast?.forecastday?.first()?.day?.avgTempC
            )
        }
    }

    @Nested
    @DisplayName("ForecastEntity.toDomain()")
    inner class ForecastEntityToDomainTest {

        @Test
        @DisplayName("GIVEN a ForecastEntity with valid serialized forecast WHEN mapping to domain THEN should return Forecast with correct data")
        fun `given ForecastEntity with valid serialized forecast when mapping to domain then returns Forecast with correct data`() {
            // Given
            val locationName = "Cartagena"
            val forecastResponseDto = createForecastResponseDto(locationName = locationName)
            val serializedJson =
                moshi.adapter(ForecastResponseDto::class.java).toJson(forecastResponseDto)
            val forecastEntity = ForecastEntity(
                locationName = locationName,
                serializedForecast = serializedJson,
                lastUpdated = System.currentTimeMillis()
            )

            // When
            val result = forecastEntity.toDomain(moshi)

            // Then
            assertEquals(locationName, result.locationName)
            assertEquals(1, result.days.size)
            assertEquals("2024-01-15", result.days[0].date)
            assertEquals(15.5, result.days[0].avgTempC)
            assertEquals("Sunny", result.days[0].conditionText)
        }

        @Test
        @DisplayName("GIVEN a ForecastEntity with invalid serialized JSON WHEN mapping to domain THEN should throw IllegalStateException")
        fun `given ForecastEntity with invalid serialized JSON when mapping to domain then throws IllegalStateException`() {
            // Given
            val forecastEntity = ForecastEntity(
                locationName = "Invalid",
                serializedForecast = "invalid json string",
                lastUpdated = System.currentTimeMillis()
            )

            // When & Then
            assertThrows(IllegalStateException::class.java) {
                forecastEntity.toDomain(moshi)
            }
        }

        @Test
        @DisplayName("GIVEN a ForecastEntity with empty serialized forecast WHEN mapping to domain THEN should throw IllegalStateException")
        fun `given ForecastEntity with empty serialized forecast when mapping to domain then throws IllegalStateException`() {
            // Given
            val forecastEntity = ForecastEntity(
                locationName = "Empty",
                serializedForecast = "",
                lastUpdated = System.currentTimeMillis()
            )

            // When & Then
            assertThrows(IllegalStateException::class.java) {
                forecastEntity.toDomain(moshi)
            }
        }

        @Test
        @DisplayName("GIVEN a ForecastEntity with multiple days in serialized forecast WHEN mapping to domain THEN should return Forecast with all days")
        fun `given ForecastEntity with multiple days when mapping to domain then returns Forecast with all days`() {
            // Given
            val locationName = "Santa Marta"
            val forecastDayDto1 = createForecastDayDto(
                date = "2024-01-21",
                avgTempC = 25.0,
                conditionText = "Sunny",
                iconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png"
            )
            val forecastDayDto2 = createForecastDayDto(
                date = "2024-01-22",
                avgTempC = 26.5,
                conditionText = "Clear",
                iconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png"
            )
            val forecastResponseDto = ForecastResponseDto(
                location = LocationDto(
                    id = 6L,
                    name = locationName,
                    region = "Magdalena",
                    country = "Colombia",
                    lat = 11.2408,
                    lon = -74.1990,
                    url = "santa-marta"
                ),
                forecast = ForecastDto(
                    forecastday = listOf(forecastDayDto1, forecastDayDto2)
                )
            )
            val serializedJson =
                moshi.adapter(ForecastResponseDto::class.java).toJson(forecastResponseDto)
            val forecastEntity = ForecastEntity(
                locationName = locationName,
                serializedForecast = serializedJson,
                lastUpdated = System.currentTimeMillis()
            )

            // When
            val result = forecastEntity.toDomain(moshi)

            // Then
            assertEquals(locationName, result.locationName)
            assertEquals(2, result.days.size)
            assertEquals("2024-01-21", result.days[0].date)
            assertEquals("2024-01-22", result.days[1].date)
        }
    }

    // Helper functions
    private fun createForecastDayDto(
        date: String,
        avgTempC: Double,
        conditionText: String,
        iconUrl: String
    ): ForecastDayDto {
        return ForecastDayDto(
            date = date,
            day = DayDto(
                avgTempC = avgTempC,
                condition = ConditionDto(
                    text = conditionText,
                    icon = iconUrl
                )
            )
        )
    }

    private fun createForecastResponseDto(locationName: String): ForecastResponseDto {
        return ForecastResponseDto(
            location = LocationDto(
                id = 1L,
                name = locationName,
                region = "Region",
                country = "Colombia",
                lat = 0.0,
                lon = 0.0,
                url = locationName.lowercase()
            ),
            forecast = ForecastDto(
                forecastday = listOf(
                    createForecastDayDto(
                        date = "2024-01-15",
                        avgTempC = 15.5,
                        conditionText = "Sunny",
                        iconUrl = "//cdn.weatherapi.com/weather/64x64/day/113.png"
                    )
                )
            )
        )
    }
}

