package com.example.mountadmin.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.repository.AdminRepository
import com.example.mountadmin.data.repository.AuthRepository
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val adminRepository = AdminRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _admin = MutableLiveData<Admin?>()
    val admin: LiveData<Admin?> = _admin

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCurrentAdmin() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getCurrentAdmin()

            result.onSuccess { admin ->
                _admin.value = admin
            }.onFailure { e ->
                _error.value = e.message
            }

            _isLoading.value = false
        }
    }

    fun updateProfile(admin: Admin) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = adminRepository.updateAdmin(admin)

            result.onSuccess {
                _updateSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to update profile"
            }

            _isLoading.value = false
        }
    }
}

