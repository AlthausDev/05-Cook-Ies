package com.althaus.dev.cookIes.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.repository.AuthRepository
import com.althaus.dev.cookIes.data.repository.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

    // ---- Gestión de Estados ----

    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val isAuthenticated: Boolean
        get() = _user.value != null

    fun resetError() {
        _errorMessage.value = null
    }

    // ---- Funciones de Autenticación ----

    fun login(email: String, password: String) = executeAuth {
        handleAuthResult(authRepository.login(email, password))
    }

    fun register(email: String, password: String) = executeAuth {
        handleAuthResult(authRepository.register(email, password))
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
            handleAuthResult(authRepository.signInWithGoogle(idToken))
        }
    }

    private fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _user.value = result.user
                _errorMessage.value = null
            }
            is AuthResult.Failure -> showError("Error: ${result.exception.message}")
            AuthResult.UserNotFound -> showError("Usuario no encontrado")
        }
    }


    fun showError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }

    // ---- Integración con Google Sign-In ----

    fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.getGoogleSignInClient()
    }

    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Int>) {
        launcher.launch(0)
    }

    // ---- Utilidades ----

    private fun executeAuth(authOperation: suspend () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            authOperation()
            _isLoading.value = false
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
}

// ---- Clase de Contrato para Google Sign-In ----

class AuthResultContract(private val googleSignInClient: GoogleSignInClient) :
    ActivityResultContract<Int, String?>() {

    override fun createIntent(context: Context, input: Int): Intent {
        return googleSignInClient.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            task.result?.idToken.also {
                Log.d("AuthResultContract", "ID Token: $it")
            }
        } else null
    }
}
