package com.example.mountadmin.ui.mountain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.repository.MountainRepository
import kotlinx.coroutines.launch

class EditMountainViewModel : ViewModel() {

    private val repository = MountainRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> = _mountain

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMountain(mountainId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getMountainById(mountainId)

            result.onSuccess { mountain ->
                _mountain.value = mountain
            }.onFailure { e ->
                _error.value = e.message
            }

            _isLoading.value = false
        }
    }

    fun updateMountain(mountain: Mountain) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.updateMountain(mountain)

            result.onSuccess {
                _saveSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to update mountain"
            }

            _isLoading.value = false
        }
    }

    fun deleteMountain(mountainId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.deleteMountain(mountainId)

            result.onSuccess {
                _deleteSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to delete mountain"
            }

            _isLoading.value = false
        }
    }
}

