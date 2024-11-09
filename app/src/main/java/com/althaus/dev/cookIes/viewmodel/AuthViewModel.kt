// viewmodel/AuthViewModel.kt
package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.repository.AuthRepository
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

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _user.value = authRepository.login(email, password)
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _user.value = authRepository.register(email, password)
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _user.value = authRepository.loginWithGoogle(idToken)
        }
    }
}
