package com.example.mounttrack.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mounttrack.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    private val _registerState = MutableLiveData<AuthState>()
    val registerState: LiveData<AuthState> = _registerState

    fun login(email: String, password: String) {
        _loginState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _loginState.value = AuthState.Success(user)
                },
                onFailure = { exception ->
                    _loginState.value = AuthState.Error(exception.message ?: "Login failed")
                }
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        _loginState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            result.fold(
                onSuccess = { user ->
                    _loginState.value = AuthState.Success(user)
                },
                onFailure = { exception ->
                    _loginState.value = AuthState.Error(exception.message ?: "Google Sign-In failed")
                }
            )
        }
    }

    fun register(fullName: String, email: String, password: String) {
        _registerState.value = AuthState.Loading

        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password)
            result.fold(
                onSuccess = { user ->
                    _registerState.value = AuthState.Success(user)
                },
                onFailure = { exception ->
                    _registerState.value = AuthState.Error(exception.message ?: "Registration failed")
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()
}

sealed class AuthState {
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

