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

class AddAdminViewModel : ViewModel() {

    private val adminRepository = AdminRepository()
    private val mountainRepository = MountainRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _mountains = MutableLiveData<List<Mountain>>()
    val mountains: LiveData<List<Mountain>> = _mountains

    private val _createSuccess = MutableLiveData<Boolean>()
    val createSuccess: LiveData<Boolean> = _createSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMountains() {
        viewModelScope.launch {
            val result = mountainRepository.getAllMountains()
            result.onSuccess { mountainList ->
                _mountains.value = mountainList
            }
        }
    }

    fun createAdmin(email: String, password: String, admin: Admin) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = adminRepository.createAdmin(email, password, admin)

            result.onSuccess {
                _createSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to create admin"
            }

            _isLoading.value = false
        }
    }
}

