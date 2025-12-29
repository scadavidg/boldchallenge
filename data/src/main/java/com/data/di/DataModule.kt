package com.data.di

import android.content.Context
import androidx.room.Room
import com.data.db.ForecastDao
import com.data.db.LocationDao
import com.data.db.LocationDatabase
import com.data.repository.ForecastRepositoryImpl
import com.data.repository.LocationRepositoryImpl
import com.domain.repository.ForecastRepository
import com.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    abstract fun bindForecastRepository(
        forecastRepositoryImpl: ForecastRepositoryImpl
    ): ForecastRepository

    companion object {
        @Provides
        @Singleton
        fun provideLocationDatabase(
            @ApplicationContext context: Context
        ): LocationDatabase {
            return Room.databaseBuilder(
                context,
                LocationDatabase::class.java,
                "location_database"
            )
                // Destructive migration acceptable for cache-only data
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        fun provideLocationDao(database: LocationDatabase): LocationDao {
            return database.locationDao()
        }

        @Provides
        fun provideForecastDao(database: LocationDatabase): ForecastDao {
            return database.forecastDao()
        }
    }
}

