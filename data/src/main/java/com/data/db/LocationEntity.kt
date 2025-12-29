package com.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val region: String?,
    val country: String,
    val lat: Double?,
    val lon: Double?,
    val url: String?,
    // Store query to enable per-query cache invalidation
    // This is a cache concern, not part of the domain model
    val query: String
)
