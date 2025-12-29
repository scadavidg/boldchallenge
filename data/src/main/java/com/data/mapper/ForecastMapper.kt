package com.data.mapper

import com.data.db.ForecastEntity
import com.data.dto.ForecastDayDto
import com.data.dto.ForecastResponseDto
import com.domain.model.Forecast
import com.domain.model.ForecastDay
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

fun ForecastResponseDto.toDomain(): Forecast {
    return Forecast(
        locationName = location.name,
        days = forecast.forecastday.map { it.toDomain() }
    )
}

fun ForecastDayDto.toDomain(): ForecastDay {
    // WeatherAPI returns relative URLs (e.g., "//cdn.weatherapi.com/...")
    // Convert to absolute URL for proper loading
    val iconUrl = if (day.condition.icon.startsWith("//")) {
        "https:${day.condition.icon}"
    } else {
        day.condition.icon
    }
    return ForecastDay(
        date = date,
        avgTempC = day.avgTempC,
        conditionText = day.condition.text,
        conditionIconUrl = iconUrl
    )
}

fun ForecastResponseDto.toEntity(moshi: Moshi, locationName: String): ForecastEntity {
    // Serialize entire DTO to JSON for storage
    // Simpler than normalizing into separate tables for cache-only data
    val adapter: JsonAdapter<ForecastResponseDto> = moshi.adapter(ForecastResponseDto::class.java)
    val json = adapter.toJson(this)
    return ForecastEntity(
        locationName = locationName,
        serializedForecast = json,
        lastUpdated = System.currentTimeMillis()
    )
}

fun ForecastEntity.toDomain(moshi: Moshi): Forecast {
    val adapter: JsonAdapter<ForecastResponseDto> = moshi.adapter(ForecastResponseDto::class.java)
    val dto = try {
        adapter.fromJson(serializedForecast)
    } catch (e: Exception) {
        // Catch all Moshi exceptions (JsonEncodingException, EOFException, JsonDataException, etc.)
        // and convert to IllegalStateException to hide implementation details
        throw IllegalStateException("Failed to deserialize forecast from cache", e)
    } ?: throw IllegalStateException("Failed to deserialize forecast from cache")
    return dto.toDomain()
}

