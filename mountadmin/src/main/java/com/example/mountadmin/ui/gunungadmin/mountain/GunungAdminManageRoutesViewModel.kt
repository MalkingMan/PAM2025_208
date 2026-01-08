package com.example.mountadmin.ui.gunungadmin.mountain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.repository.RouteCapacityRepository
import kotlinx.coroutines.launch

class GunungAdminManageRoutesViewModel : ViewModel() {

    private val repo = RouteCapacityRepository()

    private val _routes = MutableLiveData<List<HikingRoute>>(emptyList())
    val routes: LiveData<List<HikingRoute>> = _routes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun loadRoutes(mountainId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val res = repo.getRoutesWithCapacity(mountainId)
            res.onSuccess { list ->
                _routes.value = list
            }.onFailure { e ->
                _error.value = e.message
            }

            _isLoading.value = false
        }
    }

    fun addRoute(mountainId: String, route: HikingRoute) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.addOrUpdateRoute(mountainId, route)
            res.onSuccess {
                _successMessage.value = "Route saved"
                loadRoutes(mountainId)
            }.onFailure { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun updateRoute(mountainId: String, route: HikingRoute) {
        addRoute(mountainId, route)
    }

    fun deleteRoute(mountainId: String, route: HikingRoute) {
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.deleteRoute(mountainId, route)
            res.onSuccess {
                _successMessage.value = "Route deleted"
                loadRoutes(mountainId)
            }.onFailure { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun toggleRouteStatus(mountainId: String, route: HikingRoute) {
        val newStatus = if (route.status == HikingRoute.STATUS_OPEN) HikingRoute.STATUS_CLOSED else HikingRoute.STATUS_OPEN
        viewModelScope.launch {
            _isLoading.value = true
            val res = repo.updateRouteStatus(mountainId, route.routeId.ifEmpty { route.name }, newStatus)
            res.onSuccess {
                _successMessage.value = "Route status updated"
                loadRoutes(mountainId)
            }.onFailure { e ->
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}

