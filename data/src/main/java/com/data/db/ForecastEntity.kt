package com.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecasts")
data class ForecastEntity(
    @PrimaryKey
    val locationName: String,
    // Store forecast as JSON string for simplicity
    // Alternative: normalize into separate tables for production with proper relationships
    val serializedForecast: String,
    val lastUpdated: Long
)

