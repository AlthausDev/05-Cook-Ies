package com.althaus.dev.cookIes.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
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
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _user.value = result.user
                    _errorMessage.value = null
                }
                is AuthResult.Failure -> {
                    _user.value = null
                    _errorMessage.value = "Error: ${result.exception.message}"
                }
                AuthResult.UserNotFound -> {
                    _user.value = null
                    _errorMessage.value = "Inicio de sesión fallido"
                }
            }
            _isLoading.value = false
        }
    }

    fun register(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> {
                    _user.value = result.user
                    _errorMessage.value = null
                }
                is AuthResult.Failure -> {
                    _user.value = null
                    _errorMessage.value = "Error: ${result.exception.message}"
                }
                AuthResult.UserNotFound -> {
                    _user.value = null
                    _errorMessage.value = "Registro fallido"
                }
            }
            _isLoading.value = false
        }
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

    // Método para lanzar el flujo de inicio de sesión con Google
    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Int>) {
        launcher.launch(0)
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val account = task.getResult(Exception::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    val result = authRepository.signInWithGoogle(idToken)
                    when (result) {
                        is AuthResult.Success -> {
                            _user.value = result.user
                            _errorMessage.value = null

                        }
                        is AuthResult.Failure -> {
                            _user.value = null
                            _errorMessage.value = "Error: ${result.exception.message}"
                        }
                        AuthResult.UserNotFound -> {
                            _user.value = null
                            _errorMessage.value = "Usuario no encontrado"
                        }
                    }
                } else {
                    _errorMessage.value = "Error al obtener el ID Token de Google"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                when (val result = authRepository.signInWithGoogle(idToken)) {
                    is AuthResult.Success -> {
                        _user.value = result.user
                        _errorMessage.value = null
                    }
                    is AuthResult.Failure -> {
                        _user.value = null
                        _errorMessage.value = "Error: ${result.exception.message}"
                    }
                    AuthResult.UserNotFound -> {
                        _user.value = null
                        _errorMessage.value = "Usuario no encontrado"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }




    val isAuthenticated: Boolean
        get() = _user.value != null
}



// Contrato de resultado para la autenticación con Google
class AuthResultContract(private val googleSignInClient: GoogleSignInClient) :
    ActivityResultContract<Int, Task<GoogleSignInAccount>?>() {
    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
        return when (resultCode) {
            Activity.RESULT_OK -> GoogleSignIn.getSignedInAccountFromIntent(intent)
            else -> null
        }
    }

    override fun createIntent(context: Context, input: Int): Intent {
        return googleSignInClient.signInIntent.putExtra("input", input)
    }
}
