package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.model.UserProfile
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.RecipeRepository
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

    // Estado del perfil del usuario
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    // Estado de las recetas propias del usuario
    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes

    // Estados de carga y mensajes de error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUserProfile()
        loadUserRecipes()
    }

    // Cargar perfil del usuario
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val user = authRepository.currentUser
                if (user != null) {
                    _userProfile.value = UserProfile(
                        id = user.uid,
                        name = user.displayName,
                        email = user.email,
                        profileImage = user.photoUrl?.toString()
                    )
                } else {
                    _errorMessage.value = "No se pudo cargar el perfil del usuario."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar el perfil: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cargar recetas del usuario
    private fun loadUserRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = authRepository.currentUser?.uid ?: return@launch
                recipeRepository.getUserRecipes(userId).collect { recipes ->
                    _userRecipes.value = recipes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar las recetas: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Actualizar perfil del usuario
    fun updateProfile(name: String, email: String, profileImage: Any?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                val updatedProfile = UserProfile(id = userId, name = name, email = email, profileImage = profileImage?.toString())
                authRepository.updateUserProfile(updatedProfile)
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar el perfil: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Cerrar sesión
    fun logout() {
        authRepository.logout()
        _userProfile.value = null
        _userRecipes.value = emptyList()
    }
}
