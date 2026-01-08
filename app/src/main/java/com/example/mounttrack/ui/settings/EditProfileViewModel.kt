package com.example.mounttrack.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.firebase.FirebaseHelper
import com.example.mounttrack.data.model.User
import com.example.mounttrack.data.repository.UserRepository
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _updateState = MutableLiveData<UpdateState>()
    val updateState: LiveData<UpdateState> = _updateState

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

    fun updateProfile(
        fullName: String,
        phone: String,
        dob: String,
        address: String
    ) {
        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            val userId = FirebaseHelper.currentUserId
            if (userId == null) {
                _updateState.value = UpdateState.Error("User not logged in")
                return@launch
            }

            val currentUser = _userProfile.value
            if (currentUser == null) {
                _updateState.value = UpdateState.Error("Failed to load user data")
                return@launch
            }

            val updatedUser = currentUser.copy(
                fullName = fullName,
                phone = phone,
                dob = dob,
                address = address
            )

            val result = userRepository.updateUserProfile(updatedUser)
            result.fold(
                onSuccess = {
                    _userProfile.value = updatedUser
                    _updateState.value = UpdateState.Success
                },
                onFailure = { exception ->
                    _updateState.value = UpdateState.Error(
                        exception.message ?: "Failed to update profile"
                    )
                }
            )
        }
    }
}

sealed class UpdateState {
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

