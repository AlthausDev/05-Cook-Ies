package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.model.UserProfile
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.AuthResult
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
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
    private val recipeRepository: RecipeRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    // ---- Estados ----
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList()) // Estado para recetas del usuario
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes

    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites: StateFlow<List<Recipe>> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUserProfile()
        loadUserRecipes() // Cargar recetas del usuario al iniciar
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                val userData = firestoreRepository.getUser(userId)
                _userProfile.value = userData?.let {
                    UserProfile(
                        id = userId,
                        name = it["name"] as? String ?: "Usuario",
                        email = it["email"] as? String ?: "Sin correo",
                        profileImage = it["profileImage"] as? String
                    )
                }
            } catch (e: Exception) {
                showError("Error al cargar el perfil del usuario: ${e.localizedMessage}")
            }
        }
    }
    fun loadUserRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                recipeRepository.getRecipesByUser(userId).collect { result -> // Recoger el Flow
                    when (result) {
                        is RecipeResult.Success -> {
                            _userRecipes.value = result.data
                        }
                        is RecipeResult.Failure -> {
                            throw Exception(result.exception.localizedMessage)
                        }
                    }
                }
            } catch (e: Exception) {
                showError("Error al cargar las recetas: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = recipeRepository.getFavorites(userId)
            if (result is RecipeResult.Success) {
                _favorites.value = result.data
            } else {
                _favorites.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun saveUserProfile(name: String, email: String, profileImage: String?) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            firestoreRepository.saveUser(userId, name, email, profileImage)
        }
    }

    // ---- Actualización Individual ----

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

    fun updateUserEmail(newEmail: String, currentPassword: String) {
        executeWithLoading {
            val reauthResult = authRepository.reAuthenticate(currentPassword)
            if (reauthResult is AuthResult.Failure) {
                showError("Error al reautenticar: ${reauthResult.exception.localizedMessage}")
                return@executeWithLoading
            }

            val updateResult = authRepository.updateUserEmail(newEmail)
            if (updateResult is AuthResult.Success) {
                loadUserProfile() // Refrescar el perfil después del cambio
                showError("Correo actualizado exitosamente.")
            } else if (updateResult is AuthResult.Failure) {
                showError("Error al actualizar el correo: ${updateResult.exception.localizedMessage}")
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
