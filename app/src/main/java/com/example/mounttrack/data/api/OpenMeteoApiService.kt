package com.example.mounttrack.data.api

import com.example.mounttrack.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo API Service
 * https://open-meteo.com/en/docs
 * GRATIS tanpa API key!
 */
interface OpenMeteoApiService {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "relativehumidity_2m",
        @Query("timezone") timezone: String = "Asia/Jakarta"
    ): WeatherResponse

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/"
    }
}
