package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForecastResponseDto(
    @Json(name = "location")
    val location: LocationDto,
    @Json(name = "forecast")
    val forecast: ForecastDto
)

@JsonClass(generateAdapter = true)
data class ForecastDto(
    @Json(name = "forecastday")
    val forecastday: List<ForecastDayDto>
)

