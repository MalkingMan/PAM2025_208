package com.example.mountadmin.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.repository.AdminRepository
import kotlinx.coroutines.launch

class ManageAdminsViewModel : ViewModel() {
    private val adminRepository = AdminRepository()

    private val _admins = MutableLiveData<List<Admin>>()
    private val _filteredAdmins = MutableLiveData<List<Admin>>()
    val filteredAdmins: LiveData<List<Admin>> = _filteredAdmins

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _operationSuccess = MutableLiveData<String?>()
    val operationSuccess: LiveData<String?> = _operationSuccess

    private var currentFilter = FilterType.ALL
    private var searchQuery = ""

    fun loadAdmins() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = adminRepository.getAllAdmins()
            result.fold(
                onSuccess = { _admins.value = it; applyFilters() },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun setFilter(filterType: FilterType) { currentFilter = filterType; applyFilters() }
    fun setSearchQuery(query: String) { searchQuery = query; applyFilters() }

    private fun applyFilters() {
        val all = _admins.value ?: emptyList()
        var filtered = when (currentFilter) {
            FilterType.ALL -> all
            FilterType.MOUNTAIN_ADMIN -> all.filter { it.role == Admin.ROLE_MOUNTAIN_ADMIN }
            FilterType.NEWS_ADMIN -> all.filter { it.role == Admin.ROLE_NEWS_ADMIN }
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.fullName.contains(searchQuery, true) || it.email.contains(searchQuery, true) }
        }
        _filteredAdmins.value = filtered
    }

    fun toggleAdminStatus(admin: Admin) {
        viewModelScope.launch {
            val newStatus = if (admin.isActive()) Admin.STATUS_DISABLED else Admin.STATUS_ACTIVE
            adminRepository.updateAdminStatus(admin.uid, newStatus).fold(
                onSuccess = { _operationSuccess.value = "Admin status updated"; loadAdmins() },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun deleteAdmin(admin: Admin) {
        viewModelScope.launch {
            adminRepository.deleteAdmin(admin.uid).fold(
                onSuccess = { _operationSuccess.value = "Admin deleted"; loadAdmins() },
                onFailure = { _error.value = it.message }
            )
        }
    }

    fun clearMessages() { _error.value = null; _operationSuccess.value = null }

    enum class FilterType { ALL, MOUNTAIN_ADMIN, NEWS_ADMIN }
}

