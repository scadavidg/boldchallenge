package com.data.api

import com.data.dto.ForecastResponseDto
import com.data.dto.LocationDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("search.json")
    suspend fun searchLocations(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): List<LocationDto>

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") locationName: String,
        @Query("days") days: Int = 3
    ): ForecastResponseDto
}
