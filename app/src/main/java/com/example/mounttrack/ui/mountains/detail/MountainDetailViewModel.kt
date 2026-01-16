package com.example.mounttrack.ui.mountains.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.HikingRoute
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.model.MountainWeather
import com.example.mounttrack.data.repository.MountainRepository
import com.example.mounttrack.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class MountainDetailViewModel : ViewModel() {

    companion object {
        private const val TAG = "MountainDetailViewModel"
    }

    private val mountainRepository = MountainRepository()
    private val weatherRepository = WeatherRepository()

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> = _mountain

    private val _routes = MutableLiveData<List<HikingRoute>>()
    val routes: LiveData<List<HikingRoute>> = _routes

    // Weather data
    private val _weather = MutableLiveData<MountainWeather?>()
    val weather: LiveData<MountainWeather?> = _weather

    private val _isWeatherLoading = MutableLiveData<Boolean>()
    val isWeatherLoading: LiveData<Boolean> = _isWeatherLoading

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentMountainId: String = ""
    private var currentMountainName: String = ""

    fun loadMountainDetails(mountainId: String) {
        Log.d(TAG, "Loading mountain details for: $mountainId")
        _isLoading.value = true
        _error.value = null
        currentMountainId = mountainId

        viewModelScope.launch {
            val result = mountainRepository.getMountainById(mountainId)
            result.fold(
                onSuccess = { mountain ->
                    Log.d(TAG, "Mountain loaded: ${mountain.name}")
                    _mountain.value = mountain
                    currentMountainName = mountain.name

                    // Extract routes
                    val hikingRoutes = mountain.getHikingRoutes()
                    Log.d(TAG, "Found ${hikingRoutes.size} routes")
                    _routes.value = hikingRoutes

                    _isLoading.value = false

                    // Load weather data
                    loadWeatherData(mountainId, mountain.name)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading mountain: ${exception.message}")
                    _error.value = exception.message ?: "Failed to load mountain details"
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Load weather data for the mountain
     */
    private fun loadWeatherData(mountainId: String, mountainName: String) {
        viewModelScope.launch {
            _isWeatherLoading.value = true

            val result = weatherRepository.getWeatherForMountain(mountainId, mountainName)

            result.fold(
                onSuccess = { weatherData ->
                    _weather.value = weatherData
                    Log.d(TAG, "Weather loaded: ${weatherData.temperature}°C, ${weatherData.weatherStatus}")
                },
                onFailure = { error ->
                    Log.e(TAG, "Weather load failed: ${error.message}")
                    _weather.value = null
                }
            )

            _isWeatherLoading.value = false
        }
    }

    /**
     * Refresh weather data
     */
    fun refreshWeather() {
        if (currentMountainId.isNotEmpty() && currentMountainName.isNotEmpty()) {
            loadWeatherData(currentMountainId, currentMountainName)
        }
    }
}
