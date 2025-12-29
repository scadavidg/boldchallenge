package com.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocationEntity::class, ForecastEntity::class],
    version = 3,
    // Schema export disabled since we're using destructive migration for cache-only data
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun forecastDao(): ForecastDao
}
