package com.example.mountadmin.ui.mountain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.repository.MountainRepository
import kotlinx.coroutines.launch

class AddMountainViewModel : ViewModel() {

    private val repository = MountainRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun saveMountain(mountain: Mountain) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = repository.addMountain(mountain)

            result.onSuccess {
                _saveSuccess.value = true
            }.onFailure { e ->
                _error.value = e.message ?: "Failed to save mountain"
            }

            _isLoading.value = false
        }
    }
}

