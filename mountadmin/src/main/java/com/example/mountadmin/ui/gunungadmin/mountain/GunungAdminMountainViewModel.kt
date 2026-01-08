package com.example.mountadmin.ui.gunungadmin.mountain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.HikingRoute
import com.example.mountadmin.data.model.Mountain
import com.example.mountadmin.data.repository.RouteCapacityRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class GunungAdminMountainViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val capacityRepository = RouteCapacityRepository()

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> = _mountain

    private val _routes = MutableLiveData<List<HikingRoute>>(emptyList())
    val routes: LiveData<List<HikingRoute>> = _routes

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadMountainData(mountainId: String) {
        _isLoading.value = true

        // Load mountain core fields
        firestore.collection("mountains")
            .document(mountainId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val mountain = document.toObject(Mountain::class.java)?.copy(mountainId = document.id)
                    _mountain.value = mountain
                } else {
                    _errorMessage.value = "Mountain not found"
                }

                // Load routes with proper parsing (routes array maps)
                viewModelScope.launch {
                    val res = capacityRepository.getRoutesWithCapacity(mountainId)
                    res.onSuccess { list ->
                        _routes.value = list.map { r ->
                            r.copy(
                                maxCapacity = if (r.maxCapacity > 0) r.maxCapacity else 100,
                                status = if (r.status.isNotBlank()) r.status else HikingRoute.STATUS_OPEN
                            )
                        }
                    }.onFailure { e ->
                        _routes.value = emptyList()
                        _errorMessage.value = e.message
                    }

                    _isLoading.value = false
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
