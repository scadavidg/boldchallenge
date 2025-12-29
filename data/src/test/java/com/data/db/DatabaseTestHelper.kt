package com.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

/**
 * Helper class for creating in-memory databases for testing.
 * Room requires Android runtime, so tests use ApplicationProvider with isIncludeAndroidResources = true.
 *
 * Note: This requires testOptions.unitTests.isIncludeAndroidResources = true in build.gradle.kts
 */
object DatabaseTestHelper {

    /**
     * Creates an in-memory LocationDatabase for testing.
     * This database is volatile and will be destroyed when the test finishes.
     */
    fun createInMemoryDatabase(): LocationDatabase {
        // ApplicationProvider works with isIncludeAndroidResources = true
        // This provides a real Android context without needing instrumentation
        val context: Context = ApplicationProvider.getApplicationContext()
        return Room.inMemoryDatabaseBuilder(context, LocationDatabase::class.java)
            .allowMainThreadQueries() // Allow main thread for testing
            .build()
    }

    /**
     * Closes the database and cleans up resources.
     */
    fun closeDatabase(database: LocationDatabase) {
        database.close()
    }
}
