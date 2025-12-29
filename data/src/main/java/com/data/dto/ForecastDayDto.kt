package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForecastDayDto(
    @Json(name = "date")
    val date: String,
    @Json(name = "day")
    val day: DayDto
)

