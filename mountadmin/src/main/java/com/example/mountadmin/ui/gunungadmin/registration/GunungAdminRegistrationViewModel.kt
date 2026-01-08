package com.example.mountadmin.ui.gunungadmin.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Registration
import com.example.mountadmin.data.repository.RouteCapacityRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GunungAdminRegistrationViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val capacityRepository = RouteCapacityRepository()

    private val _allRegistrations = MutableLiveData<List<Registration>>(emptyList())

    private val _filteredRegistrations = MutableLiveData<List<Registration>>(emptyList())
    val filteredRegistrations: LiveData<List<Registration>> = _filteredRegistrations

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private var currentFilter: String = "ALL"
    private var mountainId: String = ""

    fun loadRegistrations(mountainId: String) {
        this.mountainId = mountainId
        _isLoading.value = true

        firestore.collection("registrations")
            .whereEqualTo("mountainId", mountainId)
            .get()
            .addOnSuccessListener { documents ->
                val registrations = documents.mapNotNull { doc ->
                    doc.toObject(Registration::class.java).copy(registrationId = doc.id)
                }.sortedByDescending { it.createdAt }
                _allRegistrations.value = registrations
                applyCurrentFilter()
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message
                _isLoading.value = false
            }
    }

    fun filterRegistrations(filter: String) {
        currentFilter = filter
        applyCurrentFilter()
    }

    private fun applyCurrentFilter() {
        val allRegs = _allRegistrations.value ?: emptyList()
        _filteredRegistrations.value = when (currentFilter) {
            "PENDING" -> allRegs.filter { it.status == Registration.STATUS_PENDING }
            "APPROVED" -> allRegs.filter { it.status == Registration.STATUS_APPROVED }
            "REJECTED" -> allRegs.filter { it.status == Registration.STATUS_REJECTED }
            "CANCELLED" -> allRegs.filter { it.status == Registration.STATUS_CANCELLED }
            else -> allRegs
        }
    }

    /**
     * Approve registration with capacity validation and atomic update.
     * This uses Firestore transaction to prevent overbooking.
     */
    fun approveRegistration(registration: Registration) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = capacityRepository.approveRegistrationWithCapacityUpdate(registration)

            result.onSuccess {
                // Update local data
                val updatedList = _allRegistrations.value?.map { reg ->
                    if (reg.registrationId == registration.registrationId) {
                        reg.copy(status = Registration.STATUS_APPROVED)
                    } else {
                        reg
                    }
                } ?: emptyList()
                _allRegistrations.value = updatedList
                applyCurrentFilter()
                _successMessage.value = "Registration approved successfully"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Failed to approve registration"
            }

            _isLoading.value = false
        }
    }

    /**
     * Reject registration - does NOT affect capacity
     */
    fun rejectRegistration(registrationId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = capacityRepository.rejectRegistration(registrationId)

            result.onSuccess {
                val updatedList = _allRegistrations.value?.map { reg ->
                    if (reg.registrationId == registrationId) {
                        reg.copy(status = Registration.STATUS_REJECTED)
                    } else {
                        reg
                    }
                } ?: emptyList()
                _allRegistrations.value = updatedList
                applyCurrentFilter()
                _successMessage.value = "Registration rejected"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Failed to reject registration"
            }

            _isLoading.value = false
        }
    }

    /**
     * Legacy method - routes to appropriate new method
     */
    fun updateRegistrationStatus(registrationId: String, newStatus: String) {
        val registration = _allRegistrations.value?.find { it.registrationId == registrationId }

        if (registration == null) {
            _errorMessage.value = "Registration not found"
            return
        }

        when (newStatus) {
            Registration.STATUS_APPROVED -> approveRegistration(registration)
            Registration.STATUS_REJECTED -> rejectRegistration(registrationId)
            Registration.STATUS_CANCELLED -> cancelRegistration(registration)
            else -> {
                // Fallback for other statuses
                viewModelScope.launch {
                    _isLoading.value = true
                    try {
                        firestore.collection("registrations")
                            .document(registrationId)
                            .update("status", newStatus)
                            .await()

                        val updatedList = _allRegistrations.value?.map { reg ->
                            if (reg.registrationId == registrationId) {
                                reg.copy(status = newStatus)
                            } else {
                                reg
                            }
                        } ?: emptyList()
                        _allRegistrations.value = updatedList
                        applyCurrentFilter()
                        _successMessage.value = "Registration updated"
                    } catch (e: Exception) {
                        _errorMessage.value = "Failed to update: ${e.message}"
                    }
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Cancel registration and restore capacity if it was approved
     */
    fun cancelRegistration(registration: Registration) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = capacityRepository.cancelRegistrationWithCapacityRestore(registration)

            result.onSuccess {
                val updatedList = _allRegistrations.value?.map { reg ->
                    if (reg.registrationId == registration.registrationId) {
                        reg.copy(status = Registration.STATUS_CANCELLED)
                    } else {
                        reg
                    }
                } ?: emptyList()
                _allRegistrations.value = updatedList
                applyCurrentFilter()
                _successMessage.value = "Registration cancelled"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Failed to cancel registration"
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }
}

