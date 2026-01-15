package com.example.mounttrack.ui.mountains.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.HikingRoute
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.repository.MountainRepository
import kotlinx.coroutines.launch

class MountainDetailViewModel : ViewModel() {

    companion object {
        private const val TAG = "MountainDetailViewModel"
    }

    private val mountainRepository = MountainRepository()

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> = _mountain

    private val _routes = MutableLiveData<List<HikingRoute>>()
    val routes: LiveData<List<HikingRoute>> = _routes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMountainDetails(mountainId: String) {
        Log.d(TAG, "Loading mountain details for: $mountainId")
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = mountainRepository.getMountainById(mountainId)
            result.fold(
                onSuccess = { mountain ->
                    Log.d(TAG, "Mountain loaded: ${mountain.name}")
                    _mountain.value = mountain

                    // Extract routes
                    val hikingRoutes = mountain.getHikingRoutes()
                    Log.d(TAG, "Found ${hikingRoutes.size} routes")
                    _routes.value = hikingRoutes

                    _isLoading.value = false
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading mountain: ${exception.message}")
                    _error.value = exception.message ?: "Failed to load mountain details"
                    _isLoading.value = false
                }
            )
        }
    }
}
