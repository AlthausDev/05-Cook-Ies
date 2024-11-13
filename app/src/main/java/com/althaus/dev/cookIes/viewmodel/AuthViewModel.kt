package com.althaus.dev.cookIes.viewmodel

import android.app.Activity
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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
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

    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun login(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            handleAuthResult(authRepository.login(email, password))
        }
    }

    fun register(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            handleAuthResult(authRepository.register(email, password))
        }
    }


    // Método consolidado para manejar el resultado de autenticación
    private fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _user.value = result.user
                _errorMessage.value = null
            }
            is AuthResult.Failure -> setErrorMessage("Error: ${result.exception.message}")
            AuthResult.UserNotFound -> setErrorMessage("Usuario no encontrado")
        }
        _isLoading.value = false
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }

    // Método para obtener GoogleSignInClient
    fun getGoogleSignInClient(): GoogleSignInClient {
        return authRepository.getGoogleSignInClient()
    }

    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Int>) {
        launcher.launch(0)
    }

    // Método para manejar el inicio de sesión con Google ID Token
    fun handleGoogleSignInResult(idToken: String?) {
        if (idToken == null) {
            setErrorMessage("Error: ID Token es nulo")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Autenticación en Firebase usando el ID Token
                val result = authRepository.signInWithGoogle(idToken)
                handleAuthResult(result)
            } catch (e: Exception) {
                Log.e("Auth", "Error en Google Sign-In: ${e.message}")
                setErrorMessage("Error en Google Sign-In: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun resetError() {
        _errorMessage.value = null
    }

    val isAuthenticated: Boolean
        get() = _user.value != null
}


class AuthResultContract(private val googleSignInClient: GoogleSignInClient) :
    ActivityResultContract<Int, String?>() {

    // Crea el Intent para iniciar la autenticación de Google
    override fun createIntent(context: Context, input: Int): Intent {
        return googleSignInClient.signInIntent
    }

    // Procesa el resultado de la autenticación de Google para obtener el ID Token
    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        return if (resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.result
            Log.d("AuthResultContract", "GoogleSignInAccount: ${account?.email}, ID Token: ${account?.idToken}")
            account?.idToken // Obtiene el `idToken` de la cuenta seleccionada
        } else null
    }

}
