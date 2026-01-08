package com.example.mountadmin.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.repository.AdminRepository
import com.example.mountadmin.data.repository.MountainRepository
import kotlinx.coroutines.launch

class EditAdminViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    private val mountainRepository = MountainRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _admin = MutableLiveData<Admin?>()
    val admin: LiveData<Admin?> = _admin

    private val _mountains = MutableLiveData<List<Mountain>>()
    val mountains: LiveData<List<Mountain>> = _mountains

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadAdmin(adminId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = adminRepository.getAdminById(adminId)
            result.onSuccess { admin ->
                _admin.value = admin
            }.onFailure { e ->
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun loadMountains() {
        viewModelScope.launch {
            val result = mountainRepository.getAllMountains()
            result.onSuccess { mountainList ->
                _mountains.value = mountainList
            }
        }
    }

    fun updateAdmin(admin: Admin) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = adminRepository.updateAdmin(admin)

            result.onSuccess {
                _updateSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to update admin"
            }

            _isLoading.value = false
        }
    }

    fun deleteAdmin(adminId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = adminRepository.deleteAdmin(adminId)

            result.onSuccess {
                _deleteSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to delete admin"
            }

            _isLoading.value = false
        }
    }
}

