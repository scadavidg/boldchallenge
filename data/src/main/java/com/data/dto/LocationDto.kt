package com.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationDto(
    @Json(name = "id")
    val id: Long? = null, // Only returned by search endpoint, not forecast
    @Json(name = "name")
    val name: String,
    @Json(name = "region")
    val region: String?,
    @Json(name = "country")
    val country: String,
    @Json(name = "lat")
    val lat: Double?,
    @Json(name = "lon")
    val lon: Double?,
    @Json(name = "url")
    val url: String? = null // Only returned by search endpoint
)
