package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.model.UserProfile
import com.althaus.dev.cookIes.data.repository.AuthRepository
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

    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes

    private val _profileStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val profileStats: StateFlow<Map<String, Any>> = _profileStats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUserProfile()
        loadUserRecipes()
        calculateProfileStats()
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

    fun updateProfile(name: String, email: String, profileImage: Any?) {
        executeWithLoading {
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                val updatedProfile = UserProfile(
                    id = userId,
                    name = name,
                    email = email,
                    profileImage = profileImage?.toString()
                )
                authRepository.updateUserProfile(updatedProfile)
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                showError("Error al actualizar el perfil: ${e.localizedMessage}")
            }
        }
    }

    // ---- Gestión de Recetas ----
    private fun loadUserRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = authRepository.currentUser?.uid ?: return@launch
                recipeRepository.getUserRecipes(userId).collect { result ->
                    when (result) {
                        is RecipeResult.Success -> _userRecipes.value = result.data
                        is RecipeResult.Failure -> _errorMessage.value = "Error al cargar las recetas: ${result.exception.localizedMessage}"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ---- Estadísticas del Perfil ----
    private fun calculateProfileStats() {
        executeWithLoading {
            try {
                val recipeCount = _userRecipes.value.size
                _profileStats.value = mapOf(
                    "recipeCount" to recipeCount,
                    "lastUpdated" to System.currentTimeMillis()
                )
            } catch (e: Exception) {
                showError("Error al calcular estadísticas del perfil: ${e.localizedMessage}")
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
