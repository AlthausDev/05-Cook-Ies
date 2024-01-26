package com.althaus.dev.project05_recetario.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.project05_recetario.repository.AuthRepository
import com.althaus.dev.project05_recetario.repository.RecipeRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // LiveData para el estado de autenticación
    //val authState = // ...

    // Método para iniciar sesión
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
        }
    }

    // Método para registrarse
    fun signUp(email: String, password: String) {
        viewModelScope.launch {
        }
    }
}
