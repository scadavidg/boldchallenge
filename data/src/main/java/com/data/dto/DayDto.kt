package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DayDto(
    @Json(name = "avgtemp_c")
    val avgTempC: Double,
    @Json(name = "condition")
    val condition: ConditionDto
)

