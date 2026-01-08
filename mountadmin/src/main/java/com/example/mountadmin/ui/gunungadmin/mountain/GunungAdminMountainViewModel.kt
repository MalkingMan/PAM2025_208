package com.example.mountadmin.ui.gunungadmin.mountain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mountadmin.data.model.Mountain
import com.google.firebase.firestore.FirebaseFirestore

class GunungAdminMountainViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _mountain = MutableLiveData<Mountain?>()
    val mountain: LiveData<Mountain?> = _mountain

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadMountainData(mountainId: String) {
        _isLoading.value = true

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

