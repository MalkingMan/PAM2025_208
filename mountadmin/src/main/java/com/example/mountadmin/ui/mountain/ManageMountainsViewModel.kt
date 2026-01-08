package com.example.mountadmin.ui.mountain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.repository.MountainRepository
import com.example.mountadmin.data.seed.MountainSeeder
import kotlinx.coroutines.launch

class ManageMountainsViewModel : ViewModel() {

    private val repository = MountainRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _mountains = MutableLiveData<List<Mountain>>()
    val mountains: LiveData<List<Mountain>> = _mountains

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _seedSuccess = MutableLiveData<Boolean>()
    val seedSuccess: LiveData<Boolean> = _seedSuccess

    private var allMountains: List<Mountain> = emptyList()

    fun loadMountains() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getAllMountains()

            result.onSuccess { mountainList ->
                allMountains = mountainList
                _mountains.value = mountainList
            }.onFailure { e ->
                _error.value = e.message
            }

            _isLoading.value = false
        }
    }

    fun searchMountains(query: String) {
        if (query.isEmpty()) {
            _mountains.value = allMountains
        } else {
            _mountains.value = allMountains.filter { mountain ->
                mountain.name.contains(query, ignoreCase = true) ||
                mountain.province.contains(query, ignoreCase = true) ||
                mountain.country.contains(query, ignoreCase = true)
            }
        }
    }

    fun deleteMountain(mountainId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteMountain(mountainId)

            result.onSuccess {
                loadMountains()
            }.onFailure { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun seedMountains(force: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _seedSuccess.value = false

            val result = MountainSeeder.seedMountains(force = force)
            result.onSuccess {
                _seedSuccess.value = true
                loadMountains()
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to seed mountains"
            }

            _isLoading.value = false
        }
    }
}
