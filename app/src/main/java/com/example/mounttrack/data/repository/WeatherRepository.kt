package com.example.mounttrack.data.repository

import android.util.Log
import com.example.mounttrack.data.api.RetrofitClient
import com.example.mounttrack.data.model.MountainWeather
import com.example.mounttrack.data.model.WeatherStatus

/**
 * Repository untuk mengambil data cuaca dari Open-Meteo API
 */
class WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"

        /**
         * Koordinat default untuk gunung-gunung Indonesia
         */
        private val MOUNTAIN_COORDINATES = mapOf(
            "merbabu" to Pair(-7.455, 110.440),
            "merapi" to Pair(-7.540, 110.446),
            "semeru" to Pair(-8.108, 112.922),
            "bromo" to Pair(-7.942, 112.953),
            "rinjani" to Pair(-8.411, 116.457),
            "slamet" to Pair(-7.242, 109.209),
            "gede" to Pair(-6.785, 106.985),
            "pangrango" to Pair(-6.790, 106.935),
            "sindoro" to Pair(-7.300, 109.992),
            "sumbing" to Pair(-7.384, 110.070),
            "lawu" to Pair(-7.625, 111.192),
            "arjuno" to Pair(-7.761, 112.589),
            "kerinci" to Pair(-1.697, 101.264),
            "agung" to Pair(-8.343, 115.508),
            "prau" to Pair(-7.188, 109.925),
            "andong" to Pair(-7.373, 110.376),
            "papandayan" to Pair(-7.320, 107.731),
            "tangkuban" to Pair(-6.759, 107.600)
        )
    }

    private val weatherApi = RetrofitClient.openMeteoApi

    /**
     * Ambil cuaca untuk satu gunung berdasarkan nama
     */
    suspend fun getWeatherForMountain(mountainId: String, mountainName: String): Result<MountainWeather> {
        return try {
            val (lat, lon) = getCoordinates(mountainName)

            val response = weatherApi.getWeather(
                latitude = lat,
                longitude = lon
            )

            val current = response.current_weather
                ?: return Result.failure(Exception("Weather data not available"))

            val humidity = response.hourly?.relativehumidity_2m?.firstOrNull() ?: 0

            val weatherStatus = WeatherStatus.fromWeatherCode(
                code = current.weathercode,
                windSpeed = current.windspeed
            )

            val weather = MountainWeather(
                mountainId = mountainId,
                mountainName = mountainName,
                temperature = current.temperature,
                windSpeed = current.windspeed,
                humidity = humidity,
                weatherCode = current.weathercode,
                weatherStatus = weatherStatus,
                weatherDescription = getWeatherDescription(current.weathercode)
            )

            Log.d(TAG, "Weather for $mountainName: ${weather.temperature}°C, ${weather.weatherStatus}")
            Result.success(weather)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for $mountainName: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun getCoordinates(mountainName: String): Pair<Double, Double> {
        val normalizedName = mountainName
            .lowercase()
            .replace("gunung ", "")
            .replace("mount ", "")
            .replace("mt. ", "")
            .replace("mt ", "")
            .trim()

        return MOUNTAIN_COORDINATES[normalizedName]
            ?: MOUNTAIN_COORDINATES.entries.find { normalizedName.contains(it.key) }?.value
            ?: Pair(-7.5, 110.0)
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45 -> "Foggy"
            48 -> "Fog with rime"
            51 -> "Light drizzle"
            53 -> "Moderate drizzle"
            55 -> "Dense drizzle"
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            71 -> "Slight snow"
            73 -> "Moderate snow"
            75 -> "Heavy snow"
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Violent rain showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
}
