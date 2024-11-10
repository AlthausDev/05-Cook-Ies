package com.althaus.dev.cookIes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Obtener el cliente de Google Sign-In
    fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.getGoogleSignInClient()
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _user.value = result.user
                    _errorMessage.value = null
                }
                is AuthResult.Failure -> {
                    _user.value = null
                    _errorMessage.value = "Error: ${result.exception.message}"
                }
                AuthResult.UserNotFound -> {
                    _user.value = null
                    _errorMessage.value = "Inicio de sesión fallido"
                }
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> {
                    _user.value = result.user
                    _errorMessage.value = null
                }
                is AuthResult.Failure -> {
                    _user.value = null
                    _errorMessage.value = "Error: ${result.exception.message}"
                }
                AuthResult.UserNotFound -> {
                    _user.value = null
                    _errorMessage.value = "Registro fallido"
                }
            }
            _isLoading.value = false
        }
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }


        fun logout() {
        authRepository.logout()
        _user.value = null
    }

    // Iniciar sesión con Google usando el idToken
    fun loginWithGoogle(idToken: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Llamamos a `signInWithGoogle` y manejamos el resultado `AuthResult`
                when (val result = authRepository.signInWithGoogle(idToken)) {
                    is AuthResult.Success -> {
                        _user.value = result.user
                        _errorMessage.value = null
                    }
                    is AuthResult.Failure -> {
                        _user.value = null
                        _errorMessage.value = "Error: ${result.exception.message}"
                    }
                    AuthResult.UserNotFound -> {
                        _user.value = null
                        _errorMessage.value = "Usuario no encontrado"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    val isAuthenticated: Boolean
        get() = _user.value != null
}
