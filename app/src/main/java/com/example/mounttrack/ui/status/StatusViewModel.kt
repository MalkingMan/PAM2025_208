package com.example.mounttrack.ui.status

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.Registration
import com.example.mounttrack.data.repository.RegistrationRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StatusViewModel : ViewModel() {

    companion object {
        private const val TAG = "StatusViewModel"
    }

    private val registrationRepository = RegistrationRepository()

    private val _currentRegistration = MutableLiveData<Registration?>()
    val currentRegistration: LiveData<Registration?> = _currentRegistration

    private val _previousRegistrations = MutableLiveData<List<Registration>>()
    val previousRegistrations: LiveData<List<Registration>> = _previousRegistrations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        startRealtimeListeners()
    }

    /**
     * Start real-time listeners for registration status.
     * Status automatically updates when admin approves/rejects registration.
     */
    private fun startRealtimeListeners() {
        val userId = FirebaseHelper.currentUserId
        if (userId == null) {
            Log.w(TAG, "User not logged in, cannot start listeners")
            _isLoading.value = false
            return
        }

        Log.d(TAG, "Starting real-time registration listeners for user: $userId")
        _isLoading.value = true

        // Real-time listener for all registrations
        registrationRepository.getUserRegistrationsRealtime(userId)
            .onEach { registrations ->
                Log.d(TAG, "Real-time registrations update: ${registrations.size} items")

                // Separate current (pending) and previous registrations
                val current = registrations.firstOrNull { it.status == Registration.STATUS_PENDING }
                val previous = registrations.filter { it.status != Registration.STATUS_PENDING }

                _currentRegistration.value = current
                _previousRegistrations.value = previous

                Log.d(TAG, "Current (pending): ${current?.registrationId ?: "none"}")
                Log.d(TAG, "Previous registrations: ${previous.size}")

                _isLoading.value = false
            }
            .catch { e ->
                Log.e(TAG, "Error in registrations real-time stream: ${e.message}")
                _currentRegistration.value = null
                _previousRegistrations.value = emptyList()
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    /**
     * Legacy method for compatibility - real-time is already active
     */
    fun loadRegistrations() {
        Log.d(TAG, "Load registrations called - real-time already active")
        // Real-time listeners are already active
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, removing listener")
        registrationRepository.removeListener()
    }
}
