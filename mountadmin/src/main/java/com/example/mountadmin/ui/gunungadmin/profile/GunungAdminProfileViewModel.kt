package com.example.mountadmin.ui.gunungadmin.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mountadmin.data.model.Admin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GunungAdminProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _adminProfile = MutableLiveData<Admin?>()
    val adminProfile: LiveData<Admin?> = _adminProfile

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadAdminProfile() {
        val currentUser = auth.currentUser ?: return
        _isLoading.value = true

        firestore.collection("admins")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val admin = document.toObject(Admin::class.java)
                    _adminProfile.value = admin
                }
                _isLoading.value = false
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

