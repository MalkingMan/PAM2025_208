package com.example.mounttrack.ui.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.Registration
import com.example.mounttrack.data.repository.RegistrationRepository
import kotlinx.coroutines.launch

class StatusViewModel : ViewModel() {

    private val registrationRepository = RegistrationRepository()

    private val _currentRegistration = MutableLiveData<Registration?>()
    val currentRegistration: LiveData<Registration?> = _currentRegistration

    private val _previousRegistrations = MutableLiveData<List<Registration>>()
    val previousRegistrations: LiveData<List<Registration>> = _previousRegistrations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadRegistrations() {
        _isLoading.value = true

        viewModelScope.launch {
            val userId = FirebaseHelper.currentUserId
            if (userId == null) {
                _isLoading.value = false
                return@launch
            }

            // Load current (pending) registration
            val currentResult = registrationRepository.getCurrentRegistration(userId)
            currentResult.fold(
                onSuccess = { registration ->
                    _currentRegistration.value = registration
                },
                onFailure = {
                    _currentRegistration.value = null
                }
            )

            // Load previous registrations
            val previousResult = registrationRepository.getPreviousRegistrations(userId)
            previousResult.fold(
                onSuccess = { registrations ->
                    _previousRegistrations.value = registrations
                },
                onFailure = {
                    _previousRegistrations.value = emptyList()
                }
            )

            _isLoading.value = false
        }
    }
}

