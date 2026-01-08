package com.example.mountadmin.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mountadmin.data.model.Admin
import com.example.mountadmin.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { admin ->
                    _loginState.value = LoginState.Success(admin)
                },
                onFailure = { exception ->
                    _loginState.value = LoginState.Error(
                        exception.message ?: "Login failed"
                    )
                }
            )
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun checkCurrentAdmin() {
        if (!authRepository.isLoggedIn()) return

        viewModelScope.launch {
            val result = authRepository.getCurrentAdmin()
            result.fold(
                onSuccess = { admin ->
                    if (admin.isActive()) {
                        _loginState.value = LoginState.Success(admin)
                    } else {
                        authRepository.logout()
                    }
                },
                onFailure = {
                    authRepository.logout()
                }
            )
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val admin: Admin) : LoginState()
    data class Error(val message: String) : LoginState()
}

