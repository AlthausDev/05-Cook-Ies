package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.model.UserProfile
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.AuthResult
import com.althaus.dev.cookIes.data.repository.RecipeRepository
import com.althaus.dev.cookIes.data.repository.RecipeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // ---- Estados ----
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUserProfile()
    }

    // ---- Gestión del Perfil ----
    private fun loadUserProfile() {
        executeWithLoading {
            try {
                val user = authRepository.currentUser
                if (user != null) {
                    _userProfile.value = UserProfile(
                        id = user.uid,
                        name = user.displayName ?: "Usuario",
                        email = user.email ?: "Sin correo",
                        profileImage = user.photoUrl?.toString()
                    )
                } else {
                    throw Exception("No se pudo cargar el perfil del usuario.")
                }
            } catch (e: Exception) {
                showError("Error al cargar el perfil: ${e.localizedMessage}")
            }
        }
    }

    // ---- Actualización Individual ----

    // Actualizar Nombre
    fun updateName(newName: String) {
        executeWithLoading {
            try {
                val currentUser = authRepository.currentUser
                    ?: throw Exception("Usuario no autenticado")
                authRepository.updateUserName(newName)
                _userProfile.value = _userProfile.value?.copy(name = newName)
            } catch (e: Exception) {
                showError("Error al actualizar el nombre: ${e.localizedMessage}")
            }
        }
    }

    // Actualizar Correo
    fun updateUserEmail(newEmail: String, currentPassword: String) {
        executeWithLoading {
            val result = authRepository.updateUserEmail(newEmail, currentPassword)
            if (result is AuthResult.Success) {
                loadUserProfile() // Refrescar perfil después del cambio
            } else if (result is AuthResult.Failure) {
                showError("Error al actualizar el correo: ${result.exception.localizedMessage}")
            }
        }
    }

    fun updateUserPassword(newPassword: String, currentPassword: String) {
        executeWithLoading {
            val result = authRepository.updateUserPassword(newPassword, currentPassword)
            if (result is AuthResult.Failure) {
                showError("Error al actualizar la contraseña: ${result.exception.localizedMessage}")
            }
        }
    }

    // ---- Utilidades para Manejo de Estados ----
    private fun showError(message: String) {
        _errorMessage.value = message
    }

    private fun executeWithLoading(operation: suspend () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Ocurrió un error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
