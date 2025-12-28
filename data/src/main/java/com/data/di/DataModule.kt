package com.data.di

import android.content.Context
import androidx.room.Room
import com.data.db.LocationDao
import com.data.db.LocationDatabase
import com.data.repository.LocationRepositoryImpl
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
                .fallbackToDestructiveMigration() // TODO: Agregar migración adecuada en producción
                .build()
        }

        @Provides
        fun provideLocationDao(database: LocationDatabase): LocationDao {
            return database.locationDao()
        }
    }
}

