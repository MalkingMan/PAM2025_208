package com.example.mounttrack.data.model

/**
 * Response dari Open-Meteo API
 * Endpoint: https://api.open-meteo.com/v1/forecast
 * 
 * Using exact JSON field names to avoid annotation issues with kapt
 */
data class WeatherResponse(
    val current_weather: CurrentWeather? = null,
    val hourly: HourlyData? = null
)

data class CurrentWeather(
    val temperature: Double = 0.0,
    val windspeed: Double = 0.0,
    val winddirection: Double = 0.0,
    val weathercode: Int = 0,
    val is_day: Int = 0,
    val time: String = ""
)

data class HourlyData(
    val relativehumidity_2m: List<Int>? = null
)

/**
 * Model untuk menampilkan cuaca di detail gunung
 */
data class MountainWeather(
    val mountainId: String = "",
    val mountainName: String = "",
    val temperature: Double = 0.0,
    val windSpeed: Double = 0.0,
    val humidity: Int = 0,
    val weatherCode: Int = 0,
    val weatherStatus: WeatherStatus = WeatherStatus.CLOUDY,
    val weatherDescription: String = ""
)

/**
 * Enum untuk status cuaca
 */
enum class WeatherStatus(val level: Int, val displayName: String) {
    CLEAR(0, "Clear"),
    CLOUDY(1, "Cloudy"),
    FOG(2, "Fog"),
    DRIZZLE(2, "Drizzle"),
    RAIN(3, "Rain"),
    HEAVY_RAIN(4, "Heavy Rain"),
    SNOW(3, "Snow"),
    THUNDERSTORM(5, "Thunderstorm"),
    STRONG_WIND(4, "Strong Wind");

    companion object {
        fun fromWeatherCode(code: Int, windSpeed: Double): WeatherStatus {
            if (windSpeed > 50) return STRONG_WIND

            return when (code) {
                0 -> CLEAR
                1, 2, 3 -> CLOUDY
                45, 48 -> FOG
                51, 53, 55 -> DRIZZLE
                56, 57 -> DRIZZLE
                61, 63 -> RAIN
                65 -> HEAVY_RAIN
                66, 67 -> RAIN
                71, 73, 75, 77 -> SNOW
                80, 81 -> RAIN
                82 -> HEAVY_RAIN
                85, 86 -> SNOW
                95, 96, 99 -> THUNDERSTORM
                else -> CLOUDY
            }
        }
    }
}
