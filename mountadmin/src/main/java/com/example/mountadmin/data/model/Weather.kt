package com.example.mountadmin.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response dari Open-Meteo API
 * Endpoint: https://api.open-meteo.com/v1/forecast
 */
data class WeatherResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeather?,
    val hourly: HourlyData?
)

data class CurrentWeather(
    val temperature: Double,
    @SerializedName("windspeed")
    val windSpeed: Double,
    @SerializedName("winddirection")
    val windDirection: Double,
    @SerializedName("weathercode")
    val weatherCode: Int,
    @SerializedName("is_day")
    val isDay: Int,
    val time: String
)

data class HourlyData(
    @SerializedName("relativehumidity_2m")
    val humidity: List<Int>?
)

/**
 * Model untuk menampilkan weather alert per gunung
 */
data class MountainWeather(
    val mountainId: String,
    val mountainName: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val windSpeed: Double,
    val humidity: Int,
    val weatherCode: Int,
    val weatherStatus: WeatherStatus,
    val weatherDescription: String
)

/**
 * Enum untuk status cuaca dengan tingkat keparahan
 */
enum class WeatherStatus(val level: Int, val displayName: String, val colorResName: String) {
    CLEAR(0, "Clear", "weather_clear"),
    CLOUDY(1, "Cloudy", "weather_cloudy"),
    FOG(2, "Fog", "weather_fog"),
    DRIZZLE(2, "Drizzle", "weather_fog"),
    RAIN(3, "Rain", "weather_rain"),
    HEAVY_RAIN(4, "Heavy Rain", "weather_rain"),
    SNOW(3, "Snow", "weather_rain"),
    THUNDERSTORM(5, "Thunderstorm", "weather_storm"),
    STRONG_WIND(4, "Strong Wind", "weather_wind");

    companion object {
        /**
         * Convert WMO Weather Code ke WeatherStatus
         * https://open-meteo.com/en/docs (WMO Weather interpretation codes)
         */
        fun fromWeatherCode(code: Int, windSpeed: Double): WeatherStatus {
            // Jika angin > 50 km/h, prioritaskan warning angin
            if (windSpeed > 50) return STRONG_WIND

            return when (code) {
                0 -> CLEAR
                1, 2, 3 -> CLOUDY
                45, 48 -> FOG
                51, 53, 55 -> DRIZZLE
                56, 57 -> DRIZZLE // Freezing drizzle
                61, 63 -> RAIN
                65 -> HEAVY_RAIN
                66, 67 -> RAIN // Freezing rain
                71, 73, 75, 77 -> SNOW
                80, 81 -> RAIN // Rain showers
                82 -> HEAVY_RAIN // Violent rain showers
                85, 86 -> SNOW // Snow showers
                95, 96, 99 -> THUNDERSTORM
                else -> CLOUDY
            }
        }
    }
}
