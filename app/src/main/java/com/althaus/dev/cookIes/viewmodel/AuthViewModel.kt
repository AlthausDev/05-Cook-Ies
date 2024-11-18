package com.althaus.dev.cookIes.viewmodel

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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

    // ---- Estados ----
    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val isAuthenticated: Boolean
        get() = _user.value != null

    // ---- Funciones ----
    fun login(email: String, password: String) = executeAuth {
        handleAuthResult(authRepository.login(email, password))
    }

    fun register(email: String, password: String, name: String?) = executeAuth {
        handleAuthResult(authRepository.register(email, password)) { user ->
            saveUserInFirestore(user, name ?: user.displayName ?: "Usuario", email)
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }

    fun handleGoogleSignInResult(idToken: String?) {
        if (idToken.isNullOrEmpty()) {
            showError("Error: ID Token es nulo")
            return
        }
        executeAuth {
            handleAuthResult(authRepository.signInWithGoogle(idToken)) { user ->
                saveUserInFirestore(
                    user = user,
                    name = user.displayName ?: "Usuario",
                    email = user.email ?: "Sin correo"
                )
            }
        }
    }

    private suspend fun saveUserInFirestore(user: FirebaseUser, name: String, email: String) {
        try {
            val profileImage = user.photoUrl?.toString()
            authRepository.firestoreRepository.saveUser(user.uid, name, email, profileImage)
        } catch (e: Exception) {
            showError("Error al guardar el usuario: ${e.localizedMessage}")
        }
    }

    // ---- Configuración de Google Sign-In ----
    fun getGoogleIdOption(context: Context): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true) // Cambiar a 'false' si deseas incluir cuentas no autorizadas
            .setServerClientId(context.getString(R.string.default_web_client_id)) // ID del cliente
            .setAutoSelectEnabled(true) // Habilitar selección automática para usuarios recurrentes
            .setNonce("secure_nonce_here") // Cambiar por un valor seguro y único por sesión
            .build()
    }

    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        // Configurar GoogleSignInOptions
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Retornar el cliente de Google Sign-In configurado
        return GoogleSignIn.getClient(activity, googleSignInOptions)
    }


    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Int>) {
        launcher.launch(0)
    }

    // ---- Manejo de Resultados ----
    private fun handleAuthResult(result: AuthResult, onSuccess: suspend (FirebaseUser) -> Unit = {}) {
        when (result) {
            is AuthResult.Success -> {
                _user.value = result.user
                _errorMessage.value = null
                viewModelScope.launch { onSuccess(result.user) }
            }
            is AuthResult.Failure -> showError("Error: ${result.exception.message}")
            AuthResult.UserNotFound -> showError("Usuario no encontrado")
        }
    }

    private fun executeAuth(authOperation: suspend () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                authOperation()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun validatePasswords(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            true
        } else {
            showError("Las contraseñas no coinciden")
            false
        }
    }

    fun resetError() {
        _errorMessage.value = null
    }

    fun showError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }
}
