package com.althaus.dev.cookIes.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.model.UserProfile
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.AuthResult
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.data.repository.RecipeRepository
import com.althaus.dev.cookIes.data.repository.RecipeResult
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
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
        loadUserRecipes()
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
            _errorMessage.value = null
            try {
                val currentUserId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                val recipes = firestoreRepository.getUserRecipes(currentUserId).map { Recipe.fromMap(it) }
                _userRecipes.value = recipes
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar recetas: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                // Subir la imagen a Firestore Storage
                val storageRef = Firebase.storage.reference.child("profile_images/${UUID.randomUUID()}.jpg")
                val uploadTask = storageRef.putFile(imageUri).await()

                // Obtener la URL de descarga
                val downloadUrl = storageRef.downloadUrl.await()

                // Actualizar la URL en el perfil del usuario
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                firestoreRepository.updateUser(userId, mapOf("profileImage" to downloadUrl.toString()))

                // Actualizar el estado local del perfil
                val updatedProfile = _userProfile.value?.copy(profileImage = downloadUrl.toString())
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar la imagen: ${e.localizedMessage}"
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

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")

                // Actualizar Firestore
                firestoreRepository.updateUser(userId, mapOf("name" to newName))

                // Actualizar el estado local
                val updatedProfile = _userProfile.value?.copy(name = newName)
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar el nombre: ${e.localizedMessage}"
            }
        }
    }


    fun updateUserEmail(newEmail: String, currentPassword: String) {
        executeWithLoading {
            val result = authRepository.updateUserEmail(newEmail, currentPassword)
            when (result) {
                is AuthResult.Success -> {
                    _userProfile.value = _userProfile.value?.copy(email = newEmail) // Actualizar email localmente
                    showError("Correo actualizado con éxito.")
                }
                is AuthResult.Failure -> showError("Error al actualizar el correo: ${result.exception.localizedMessage}")
                AuthResult.UserNotFound -> showError("Usuario no autenticado.")
            }
        }
    }


    fun updateUserPassword(newPassword: String, currentPassword: String) {
        viewModelScope.launch {
            try {
                val reauthResult = authRepository.reAuthenticate(currentPassword)
                if (reauthResult is AuthResult.Success) {
                    val updateResult = authRepository.updateUserPassword(newPassword, currentPassword)
                    if (updateResult is AuthResult.Success) {
                        _errorMessage.value = "Contraseña actualizada con éxito."
                    } else if (updateResult is AuthResult.Failure) {
                        throw Exception(updateResult.exception.localizedMessage)
                    }
                } else if (reauthResult is AuthResult.Failure) {
                    throw Exception(reauthResult.exception.localizedMessage)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar la contraseña: ${e.localizedMessage}"
            }
        }
    }


    // ---- Utilidades para Manejo de Estados ----
    fun showError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
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
