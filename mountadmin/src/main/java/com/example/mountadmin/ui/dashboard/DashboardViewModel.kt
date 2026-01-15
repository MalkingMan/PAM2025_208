package com.example.mountadmin.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.model.DashboardStats
import com.example.mountadmin.data.model.MountainWeather
import com.example.mountadmin.data.repository.AuthRepository
import com.example.mountadmin.data.repository.DashboardRepository
import com.example.mountadmin.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val dashboardRepository = DashboardRepository()
    private val authRepository = AuthRepository()
    private val weatherRepository = WeatherRepository()

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> = _dashboardStats

    private val _currentAdmin = MutableLiveData<Admin?>()
    val currentAdmin: LiveData<Admin?> = _currentAdmin

    private val _weatherAlerts = MutableLiveData<List<MountainWeather>>()
    val weatherAlerts: LiveData<List<MountainWeather>> = _weatherAlerts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isWeatherLoading = MutableLiveData<Boolean>()
    val isWeatherLoading: LiveData<Boolean> = _isWeatherLoading

    private val _weatherError = MutableLiveData<String?>()
    val weatherError: LiveData<String?> = _weatherError

    fun loadDashboardData() {
        _isLoading.value = true

        viewModelScope.launch {
            val adminResult = authRepository.getCurrentAdmin()
            adminResult.fold(
                onSuccess = { admin -> _currentAdmin.value = admin },
                onFailure = { _currentAdmin.value = null }
            )

            val statsResult = dashboardRepository.getDashboardStats()
            statsResult.fold(
                onSuccess = { stats -> _dashboardStats.value = stats },
                onFailure = { _dashboardStats.value = DashboardStats() }
            )

            _isLoading.value = false

            // Load weather data (separate loading state)
            loadWeatherData()
        }
    }

    /**
     * Load weather alerts for all mountains
     */
    fun loadWeatherData() {
        viewModelScope.launch {
            _isWeatherLoading.value = true
            _weatherError.value = null

            val result = weatherRepository.getAllMountainsWeather()

            result.fold(
                onSuccess = { weatherList ->
                    _weatherAlerts.value = weatherList
                },
                onFailure = { error ->
                    _weatherError.value = error.message ?: "Failed to load weather data"
                    _weatherAlerts.value = emptyList()
                }
            )

            _isWeatherLoading.value = false
        }
    }

    /**
     * Refresh weather data only
     */
    fun refreshWeather() {
        loadWeatherData()
    }

    fun clearWeatherError() {
        _weatherError.value = null
    }

    fun logout() {
        authRepository.logout()
    }
}
