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
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel para la gestión del perfil de usuario.
 *
 * Este ViewModel se encarga de manejar las acciones relacionadas con el perfil del usuario, como cargar su información,
 * actualizar datos personales y gestionar recetas favoritas o creadas por el usuario.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val recipeRepository: RecipeRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    // ---- Estados ----

    /**
     * Flujo de estado que contiene la información del perfil del usuario.
     */
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    /**
     * Flujo de estado que contiene las recetas creadas por el usuario.
     */
    private val _userRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val userRecipes: StateFlow<List<Recipe>> = _userRecipes

    /**
     * Flujo de estado que contiene las recetas favoritas del usuario.
     */
    private val _favorites = MutableStateFlow<List<Recipe>>(emptyList())
    val favorites: StateFlow<List<Recipe>> = _favorites

    /**
     * Flujo de estado que indica si hay una operación en progreso.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Flujo de estado que contiene mensajes de error.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Inicializa el ViewModel cargando el perfil y las recetas del usuario.
     */
    init {
        loadUserProfile()
        loadUserRecipes()
    }

    // ---- Funciones principales ----

    /**
     * Carga la información del perfil del usuario desde Firestore.
     */
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

    /**
     * Carga las recetas creadas por el usuario desde Firestore.
     */
    fun loadUserRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                val recipes = firestoreRepository.getUserRecipes(currentUserId).map { Recipe.fromMap(it) }
                _userRecipes.value = recipes
            } catch (e: Exception) {
                showError("Error al cargar recetas: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza la imagen de perfil del usuario en Firestore y Firebase Storage.
     *
     * @param imageUri URI de la nueva imagen de perfil.
     */
    fun updateProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                val storageRef = Firebase.storage.reference.child("profile_images/${UUID.randomUUID()}.jpg")
                storageRef.putFile(imageUri).await()
                val downloadUrl = storageRef.downloadUrl.await()

                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                firestoreRepository.updateUser(userId, mapOf("profileImage" to downloadUrl.toString()))
                _userProfile.value = _userProfile.value?.copy(profileImage = downloadUrl.toString())
            } catch (e: Exception) {
                showError("Error al actualizar la imagen: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Carga las recetas favoritas del usuario desde Firestore.
     *
     * @param userId ID del usuario.
     */
    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = recipeRepository.getFavorites(userId)
                if (result is RecipeResult.Success) {
                    _favorites.value = result.data
                } else {
                    _favorites.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda un perfil de usuario actualizado en Firestore.
     *
     * @param name Nuevo nombre del usuario.
     * @param email Nuevo correo electrónico del usuario.
     * @param profileImage URL de la nueva imagen de perfil.
     */
    fun saveUserProfile(name: String, email: String, profileImage: String?) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            firestoreRepository.saveUser(userId, name, email, profileImage)
        }
    }

    /**
     * Actualiza el nombre del usuario en Firestore.
     *
     * @param newName Nuevo nombre del usuario.
     */
    fun updateUserName(newName: String) {
        viewModelScope.launch {
            try {
                val userId = authRepository.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                firestoreRepository.updateUser(userId, mapOf("name" to newName))
                _userProfile.value = _userProfile.value?.copy(name = newName)
            } catch (e: Exception) {
                showError("Error al actualizar el nombre: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Actualiza el correo electrónico del usuario en Firebase Authentication y Firestore.
     *
     * @param newEmail Nuevo correo electrónico.
     * @param currentPassword Contraseña actual para la autenticación.
     */
    fun updateUserEmail(newEmail: String, currentPassword: String) {
        executeWithLoading {
            val result = authRepository.updateUserEmail(newEmail, currentPassword)
            when (result) {
                is AuthResult.Success -> {
                    _userProfile.value = _userProfile.value?.copy(email = newEmail)
                }
                is AuthResult.Failure -> showError("Error al actualizar el correo: ${result.exception.localizedMessage}")
                AuthResult.UserNotFound -> showError("Usuario no autenticado.")
            }
        }
    }

    /**
     * Actualiza la contraseña del usuario.
     *
     * @param newPassword Nueva contraseña.
     * @param currentPassword Contraseña actual.
     */
    fun updateUserPassword(newPassword: String, currentPassword: String) {
        executeWithLoading {
            val reauthResult = authRepository.reAuthenticate(currentPassword)
            if (reauthResult is AuthResult.Success) {
                val updateResult = authRepository.updateUserPassword(newPassword, currentPassword)
                if (updateResult is AuthResult.Failure) {
                    showError("Error al actualizar la contraseña: ${updateResult.exception.localizedMessage}")
                }
            } else if (reauthResult is AuthResult.Failure) {
                showError("Error en la reautenticación: ${reauthResult.exception.localizedMessage}")
            }
        }
    }

    // ---- Manejo de errores y utilidades ----

    /**
     * Establece un mensaje de error.
     *
     * @param message Mensaje de error a mostrar.
     */
    fun showError(message: String) {
        _errorMessage.value = message
    }

    /**
     * Limpia el mensaje de error actual.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Ejecuta una operación en un estado de carga.
     *
     * @param operation Operación suspendida a ejecutar.
     */
    private fun executeWithLoading(operation: suspend () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                showError("Ocurrió un error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
