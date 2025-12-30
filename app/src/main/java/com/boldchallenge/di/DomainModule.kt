package com.boldchallenge.di

import com.domain.repository.ForecastRepository
import com.domain.repository.LocationRepository
import com.domain.usecase.GetForecastUseCase
import com.domain.usecase.SearchLocationsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideSearchLocationsUseCase(
        locationRepository: LocationRepository
    ): SearchLocationsUseCase {
        return SearchLocationsUseCase(locationRepository)
    }

    @Provides
    @Singleton
    fun provideGetForecastUseCase(
        forecastRepository: ForecastRepository
    ): GetForecastUseCase {
        return GetForecastUseCase(forecastRepository)
    }
}

