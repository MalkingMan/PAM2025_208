package com.example.mountadmin.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.model.DashboardStats
import com.example.mountadmin.data.repository.AuthRepository
import com.example.mountadmin.data.repository.DashboardRepository
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val dashboardRepository = DashboardRepository()
    private val authRepository = AuthRepository()

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> = _dashboardStats

    private val _currentAdmin = MutableLiveData<Admin?>()
    val currentAdmin: LiveData<Admin?> = _currentAdmin

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadDashboardData() {
        _isLoading.value = true

        viewModelScope.launch {
            val adminResult = authRepository.getCurrentAdmin()
            adminResult.fold(
                onSuccess = { admin -> _currentAdmin.value = admin },
                onFailure = { _currentAdmin.value = null }
            )

            val statsResult = dashboardRepository.getDashboardStats()
            statsResult.fold(
                onSuccess = { stats -> _dashboardStats.value = stats },
                onFailure = { _dashboardStats.value = DashboardStats() }
            )

            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

