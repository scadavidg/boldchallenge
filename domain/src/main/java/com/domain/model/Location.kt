package com.domain.model

data class Location(
    val id: Long?, // Nullable: only available from search endpoint, not forecast
    val name: String,
    val region: String?,
    val country: String,
    val lat: Double?,
    val lon: Double?,
    val url: String?
)
