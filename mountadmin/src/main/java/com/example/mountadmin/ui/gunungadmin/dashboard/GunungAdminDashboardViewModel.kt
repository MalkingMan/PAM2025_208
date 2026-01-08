package com.example.mountadmin.ui.gunungadmin.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.model.Registration
import com.google.firebase.firestore.FirebaseFirestore

class GunungAdminDashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

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

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadDashboardData(mountainId: String) {
        _isLoading.value = true

        // Load mountain data
        loadMountainData(mountainId)

        // Load registration statistics
        loadRegistrationStats(mountainId)

        // Load recent registrations
        loadRecentRegistrations(mountainId)
    }

    private fun loadMountainData(mountainId: String) {
        firestore.collection("mountains")
            .document(mountainId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val mountain = document.toObject(Mountain::class.java)?.copy(mountainId = document.id)
                    _mountain.value = mountain

                    // Calculate route capacities
                    mountain?.routes?.let { routes ->
                        calculateRouteCapacities(mountainId, routes)
                    }
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
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

    private fun calculateRouteCapacities(mountainId: String, routes: List<HikingRoute>) {
        val capacities = mutableListOf<RouteCapacity>()
        val maxCapacity = 150 // Default max capacity per route

        firestore.collection("registrations")
            .whereEqualTo("mountainId", mountainId)
            .get()
            .addOnSuccessListener { documents ->
                val registrations = documents.mapNotNull { doc ->
                    doc.toObject(Registration::class.java)
                }.filter { it.status == Registration.STATUS_APPROVED }

                routes.forEach { route ->
                    val routeRegistrations = registrations.count { it.route == route.name }
                    val percentage = (routeRegistrations.toFloat() / maxCapacity * 100).toInt()
                    val status = when {
                        percentage >= 90 -> "Full"
                        percentage >= 70 -> "Limited Slots"
                        else -> "Available"
                    }
                    capacities.add(
                        RouteCapacity(
                            routeName = route.name,
                            currentCount = routeRegistrations,
                            maxCapacity = maxCapacity,
                            percentage = percentage,
                            status = status
                        )
                    )
                }
                _routeCapacities.value = capacities
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

data class RouteCapacity(
    val routeName: String,
    val currentCount: Int,
    val maxCapacity: Int,
    val percentage: Int,
    val status: String
)

