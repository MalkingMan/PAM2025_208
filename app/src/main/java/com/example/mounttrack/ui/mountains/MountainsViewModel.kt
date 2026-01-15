package com.example.mounttrack.ui.mountains

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.model.Mountain
import com.example.mounttrack.data.repository.MountainRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        startRealtimeListener()
    }

    /**
     * Start real-time listener for mountains.
     * Data automatically updates when admin adds/edits/deletes mountains.
     */
    private fun startRealtimeListener() {
        Log.d(TAG, "Starting real-time mountains listener...")
        _isLoading.value = true
        _error.value = null

        mountainRepository.getMountainsRealtime()
            .onEach { mountainList ->
                Log.d(TAG, "Real-time update: ${mountainList.size} mountains")
                _mountains.value = mountainList
                _isLoading.value = false
            }
            .catch { exception ->
                Log.e(TAG, "Error in mountains real-time stream: ${exception.message}")
                _error.value = exception.message ?: "Failed to load mountains"
                _mountains.value = emptyList()
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    fun refreshMountains() {
        Log.d(TAG, "Manual refresh requested - real-time already active")
        // Real-time listener is already active
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, removing listener")
        mountainRepository.removeListener()
    }
}
