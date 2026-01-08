package com.example.mountadmin.ui.gunungadmin.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mountadmin.data.model.Registration
import com.google.firebase.firestore.FirebaseFirestore

class GunungAdminRegistrationViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

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
            else -> allRegs
        }
    }

    fun updateRegistrationStatus(registrationId: String, newStatus: String) {
        _isLoading.value = true

        firestore.collection("registrations")
            .document(registrationId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Update local data
                val updatedList = _allRegistrations.value?.map { reg ->
                    if (reg.registrationId == registrationId) {
                        reg.copy(status = newStatus)
                    } else {
                        reg
                    }
                } ?: emptyList()
                _allRegistrations.value = updatedList
                applyCurrentFilter()

                val statusText = when (newStatus) {
                    Registration.STATUS_APPROVED -> "approved"
                    Registration.STATUS_REJECTED -> "rejected"
                    else -> "updated"
                }
                _successMessage.value = "Registration $statusText successfully"
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to update: ${e.message}"
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

