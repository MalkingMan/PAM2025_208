package com.example.mountadmin.data.repository

import android.util.Log
import com.example.mountadmin.data.api.RetrofitClient
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.model.MountainWeather
import com.example.mountadmin.data.model.WeatherStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengambil data cuaca dari Open-Meteo API
 * dan menggabungkannya dengan data gunung dari Firestore
 */
class WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"

        /**
         * Koordinat default untuk gunung-gunung Indonesia
         * Digunakan jika data koordinat tidak tersedia di Firestore
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

    private val firestore = FirebaseFirestore.getInstance()
    private val weatherApi = RetrofitClient.openMeteoApi

    /**
     * Ambil cuaca untuk semua gunung yang aktif
     */
    suspend fun getAllMountainsWeather(): Result<List<MountainWeather>> {
        return try {
            // 1. Ambil semua gunung dari Firestore
            val mountainsSnapshot = firestore.collection("mountains")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val mountains = mountainsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Mountain::class.java)?.copy(mountainId = doc.id)
            }

            if (mountains.isEmpty()) {
                Log.d(TAG, "No active mountains found")
                return Result.success(emptyList())
            }

            // 2. Fetch cuaca untuk setiap gunung secara parallel
            val weatherList = coroutineScope {
                mountains.map { mountain ->
                    async {
                        fetchWeatherForMountain(mountain)
                    }
                }.awaitAll()
            }.filterNotNull()

            // 3. Sort by severity (highest first)
            val sortedWeather = weatherList.sortedByDescending { it.weatherStatus.level }

            Log.d(TAG, "Fetched weather for ${sortedWeather.size} mountains")
            Result.success(sortedWeather)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch cuaca untuk satu gunung
     */
    private suspend fun fetchWeatherForMountain(mountain: Mountain): MountainWeather? {
        return try {
            // Dapatkan koordinat (dari mapping atau estimasi)
            val (lat, lon) = getCoordinates(mountain)

            // Call Open-Meteo API
            val response = weatherApi.getWeather(
                latitude = lat,
                longitude = lon
            )

            val current = response.currentWeather ?: return null
            val humidity = response.hourly?.humidity?.firstOrNull() ?: 0

            val weatherStatus = WeatherStatus.fromWeatherCode(
                code = current.weatherCode,
                windSpeed = current.windSpeed
            )

            MountainWeather(
                mountainId = mountain.mountainId,
                mountainName = mountain.name,
                latitude = lat,
                longitude = lon,
                temperature = current.temperature,
                windSpeed = current.windSpeed,
                humidity = humidity,
                weatherCode = current.weatherCode,
                weatherStatus = weatherStatus,
                weatherDescription = getWeatherDescription(current.weatherCode)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for ${mountain.name}: ${e.message}")
            null
        }
    }

    /**
     * Dapatkan koordinat gunung
     * Prioritas: data Firestore > mapping hardcoded > default
     */
    private fun getCoordinates(mountain: Mountain): Pair<Double, Double> {
        // TODO: Tambahkan field latitude/longitude ke model Mountain jika diperlukan
        // Untuk sekarang, gunakan mapping berdasarkan nama

        val normalizedName = mountain.name
            .lowercase()
            .replace("gunung ", "")
            .replace("mount ", "")
            .replace("mt. ", "")
            .replace("mt ", "")
            .trim()

        return MOUNTAIN_COORDINATES[normalizedName]
            ?: MOUNTAIN_COORDINATES.entries.find { normalizedName.contains(it.key) }?.value
            ?: Pair(-7.5, 110.0) // Default: Central Java
    }

    /**
     * Convert WMO Weather Code ke deskripsi yang mudah dibaca
     */
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
            56 -> "Light freezing drizzle"
            57 -> "Dense freezing drizzle"
            61 -> "Slight rain"
            63 -> "Moderate rain"
            65 -> "Heavy rain"
            66 -> "Light freezing rain"
            67 -> "Heavy freezing rain"
            71 -> "Slight snow"
            73 -> "Moderate snow"
            75 -> "Heavy snow"
            77 -> "Snow grains"
            80 -> "Slight rain showers"
            81 -> "Moderate rain showers"
            82 -> "Violent rain showers"
            85 -> "Slight snow showers"
            86 -> "Heavy snow showers"
            95 -> "Thunderstorm"
            96 -> "Thunderstorm with slight hail"
            99 -> "Thunderstorm with heavy hail"
            else -> "Unknown"
        }
    }
}
