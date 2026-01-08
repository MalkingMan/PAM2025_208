package com.example.mountadmin.ui.gunungadmin.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.model.Registration
import com.example.mountadmin.data.repository.RouteCapacityRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GunungAdminDashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val capacityRepository = RouteCapacityRepository()

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> = _mountain

    private val _totalRegistrations = MutableLiveData(0)
    val totalRegistrations: LiveData<Int> = _totalRegistrations

    private val _pendingCount = MutableLiveData(0)
    val pendingCount: LiveData<Int> = _pendingCount

    private val _approvedCount = MutableLiveData(0)
    val approvedCount: LiveData<Int> = _approvedCount

    private val _rejectedCount = MutableLiveData(0)
    val rejectedCount: LiveData<Int> = _rejectedCount

    private val _recentRegistrations = MutableLiveData<List<Registration>>(emptyList())
    val recentRegistrations: LiveData<List<Registration>> = _recentRegistrations

    private val _routeCapacities = MutableLiveData<List<RouteCapacity>>(emptyList())
    val routeCapacities: LiveData<List<RouteCapacity>> = _routeCapacities

    private val _routeRegistrationChart = MutableLiveData<List<RouteRegistrationCount>>(emptyList())
    val routeRegistrationChart: LiveData<List<RouteRegistrationCount>> = _routeRegistrationChart

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadDashboardData(mountainId: String) {
        _isLoading.value = true

        // Load mountain data with real capacity
        loadMountainDataWithCapacity(mountainId)

        // Load registration statistics
        loadRegistrationStats(mountainId)

        // Load recent registrations
        loadRecentRegistrations(mountainId)

        // Load chart data
        loadRouteRegistrationChart(mountainId)
    }

    private fun loadMountainDataWithCapacity(mountainId: String) {
        viewModelScope.launch {
            val result = capacityRepository.getRoutesWithCapacity(mountainId)

            result.onSuccess { routes ->
                // Load full mountain data
                firestore.collection("mountains")
                    .document(mountainId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val mountain = document.toObject(Mountain::class.java)?.copy(mountainId = document.id)
                            _mountain.value = mountain

                            // Build route capacities from REAL Firestore data
                            buildRouteCapacities(routes)
                        }
                        _isLoading.value = false
                    }
                    .addOnFailureListener { e ->
                        _errorMessage.value = e.message
                        _isLoading.value = false
                    }
            }.onFailure { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Build route capacity display data from actual HikingRoute data (NOT hardcoded)
     */
    private fun buildRouteCapacities(routes: List<HikingRoute>) {
        val capacities = routes.map { route ->
            val percentage = if (route.maxCapacity > 0) {
                (route.usedCapacity.toFloat() / route.maxCapacity * 100).toInt()
            } else {
                0
            }

            val status = when {
                route.status == HikingRoute.STATUS_CLOSED -> "Closed"
                route.isFull -> "Full"
                percentage >= 90 -> "Almost Full"
                percentage >= 70 -> "Limited Slots"
                else -> "Available"
            }

            RouteCapacity(
                routeId = route.routeId,
                routeName = route.name,
                currentCount = route.usedCapacity,
                maxCapacity = route.maxCapacity,
                percentage = percentage,
                status = status,
                routeStatus = route.status
            )
        }
        _routeCapacities.value = capacities
    }

    private fun loadRegistrationStats(mountainId: String) {
        firestore.collection("registrations")
            .whereEqualTo("mountainId", mountainId)
            .get()
            .addOnSuccessListener { documents ->
                val registrations = documents.mapNotNull { doc ->
                    doc.toObject(Registration::class.java)
                }

                _totalRegistrations.value = registrations.size
                _pendingCount.value = registrations.count { it.status == Registration.STATUS_PENDING }
                _approvedCount.value = registrations.count { it.status == Registration.STATUS_APPROVED }
                _rejectedCount.value = registrations.count { it.status == Registration.STATUS_REJECTED }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
            }
    }

    private fun loadRecentRegistrations(mountainId: String) {
        firestore.collection("registrations")
            .whereEqualTo("mountainId", mountainId)
            .get()
            .addOnSuccessListener { documents ->
                val registrations = documents.mapNotNull { doc ->
                    doc.toObject(Registration::class.java).copy(registrationId = doc.id)
                }.sortedByDescending { it.createdAt }.take(5)
                _recentRegistrations.value = registrations
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
            }
    }

    private fun loadRouteRegistrationChart(mountainId: String) {
        viewModelScope.launch {
            try {
                // Prefer route list from mountain (labels)
                val routes = capacityRepository.getRoutesWithCapacity(mountainId).getOrElse { emptyList() }

                val snapshot = firestore.collection("registrations")
                    .whereEqualTo("mountainId", mountainId)
                    .get()
                    .await()

                val countsByKey = mutableMapOf<String, Int>()
                snapshot.documents.forEach { doc ->
                    val routeId = doc.getString("routeId").orEmpty()
                    val routeName = doc.getString("route").orEmpty()
                    val key = routeId.ifEmpty { routeName }
                    if (key.isNotBlank()) {
                        countsByKey[key] = (countsByKey[key] ?: 0) + 1
                    }
                }

                // Build chart list in a stable order: map known routes first, then unknown
                val known = routes.mapNotNull { r ->
                    val key = r.routeId.ifEmpty { r.name }
                    val count = countsByKey[key] ?: countsByKey[r.name] ?: 0
                    RouteRegistrationCount(
                        key = key,
                        routeId = r.routeId,
                        routeName = r.name,
                        count = count
                    )
                }

                // Include registrations that reference deleted/unknown routes
                val knownKeys = known.map { it.key }.toSet()
                val unknown = countsByKey
                    .filterKeys { it.isNotBlank() && !knownKeys.contains(it) }
                    .map { (key, count) ->
                        RouteRegistrationCount(
                            key = key,
                            routeId = "",
                            routeName = key,
                            count = count
                        )
                    }

                val merged = (known + unknown)
                    .filter { it.count > 0 }
                    .sortedByDescending { it.count }

                _routeRegistrationChart.value = merged
            } catch (e: Exception) {
                Log.e("GunungAdminDashboardVM", "Error loading route chart: ${e.message}", e)
                // fail silently but keep an empty chart
                _routeRegistrationChart.value = emptyList()
            }
        }
    }

    /**
     * Update route status (open/closed)
     */
    fun updateRouteStatus(mountainId: String, routeId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = capacityRepository.updateRouteStatus(mountainId, routeId, newStatus)

            result.onSuccess {
                // Reload to get updated data
                loadDashboardData(mountainId)
            }.onFailure { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class RouteCapacity(
    val routeId: String = "",
    val routeName: String,
    val currentCount: Int,
    val maxCapacity: Int,
    val percentage: Int,
    val status: String,
    val routeStatus: String = HikingRoute.STATUS_OPEN
)

data class RouteRegistrationCount(
    val key: String,
    val routeId: String,
    val routeName: String,
    val count: Int
)
