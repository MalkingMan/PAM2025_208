package com.example.mounttrack.ui.mountains

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.repository.MountainRepository
import kotlinx.coroutines.launch

class MountainsViewModel : ViewModel() {

    companion object {
        private const val TAG = "MountainsViewModel"
    }

    private val mountainRepository = MountainRepository()

    private val _mountains = MutableLiveData<List<Mountain>>()
    val mountains: LiveData<List<Mountain>> = _mountains

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadMountains()
    }

    fun loadMountains() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            Log.d(TAG, "Loading mountains...")
            val result = mountainRepository.getMountains()
            result.fold(
                onSuccess = { mountainList ->
                    Log.d(TAG, "Loaded ${mountainList.size} mountains")
                    _mountains.value = mountainList
                    _isLoading.value = false
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading mountains: ${exception.message}")
                    _error.value = exception.message ?: "Failed to load mountains"
                    _mountains.value = emptyList()
                    _isLoading.value = false
                }
            )
        }
    }

    fun refreshMountains() {
        loadMountains()
    }
}

