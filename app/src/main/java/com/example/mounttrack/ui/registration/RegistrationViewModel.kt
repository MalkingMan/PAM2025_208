package com.example.mounttrack.ui.registration

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.HikingRoute
import com.example.mounttrack.data.model.Registration
import com.example.mounttrack.data.model.User
import com.example.mounttrack.data.repository.MountainRepository
import com.example.mounttrack.data.repository.RegistrationRepository
import com.example.mounttrack.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {

    companion object {
        private const val TAG = "RegistrationViewModel"
    }

    private val registrationRepository = RegistrationRepository()
    private val userRepository = UserRepository()
    private val mountainRepository = MountainRepository()

    private val _registrationState = MutableLiveData<RegistrationState>()
    val registrationState: LiveData<RegistrationState> = _registrationState

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _mountainRoutes = MutableLiveData<List<HikingRoute>>()
    val mountainRoutes: LiveData<List<HikingRoute>> = _mountainRoutes

    fun loadUserProfile() {
        viewModelScope.launch {
            val userId = FirebaseHelper.currentUserId ?: return@launch
            val result = userRepository.getUserProfile(userId)
            result.fold(
                onSuccess = { user ->
                    _userProfile.value = user
                },
                onFailure = {
                    _userProfile.value = null
                }
            )
        }
    }

    fun loadMountainRoutes(mountainId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Loading routes for mountain: $mountainId")
            val result = mountainRepository.getMountainById(mountainId)
            result.fold(
                onSuccess = { mountain ->
                    // Extract HikingRoute objects from the mountain
                    val hikingRoutes = mountain.getHikingRoutes()
                    Log.d(TAG, "Found ${hikingRoutes.size} routes with difficulty levels")
                    _mountainRoutes.value = hikingRoutes
                },
                onFailure = { exception ->
                    Log.e(TAG, "Error loading mountain routes: ${exception.message}")
                    _mountainRoutes.value = emptyList()
                }
            )
        }
    }

    fun submitRegistration(
        mountainId: String,
        mountainName: String,
        fullName: String,
        dob: String,
        address: String,
        phone: String,
        email: String,
        route: String,
        idCardUri: String
    ) {
        _registrationState.value = RegistrationState.Loading

        viewModelScope.launch {
            val userId = FirebaseHelper.currentUserId
            if (userId == null) {
                _registrationState.value = RegistrationState.Error("User not logged in")
                return@launch
            }

            val registration = Registration(
                userId = userId,
                mountainId = mountainId,
                mountainName = mountainName,
                fullName = fullName,
                dob = dob,
                address = address,
                phone = phone,
                email = email,
                route = route,
                idCardUri = idCardUri,
                status = Registration.STATUS_PENDING
            )

            val result = registrationRepository.createRegistration(registration)
            result.fold(
                onSuccess = { registrationId ->
                    _registrationState.value = RegistrationState.Success(registrationId)
                },
                onFailure = { exception ->
                    _registrationState.value = RegistrationState.Error(
                        exception.message ?: "Registration failed"
                    )
                }
            )
        }
    }
}

sealed class RegistrationState {
    object Loading : RegistrationState()
    data class Success(val registrationId: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

