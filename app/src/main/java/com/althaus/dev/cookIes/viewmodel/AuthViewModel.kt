package com.althaus.dev.cookIes.viewmodel

import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
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

/**
 * ViewModel que maneja la lógica de autenticación de la aplicación.
 *
 * Esta clase gestiona el inicio de sesión, registro, restablecimiento de contraseña e integración con Google Sign-In.
 * Interactúa con el [AuthRepository] para realizar operaciones de autenticación y mantiene flujos de estado
 * para que los componentes de UI observen cambios.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // ---- Estados ----

    /**
     * StateFlow que representa al [FirebaseUser] actualmente autenticado.
     */
    private val _user = MutableStateFlow<FirebaseUser?>(authRepository.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    /**
     * StateFlow que indica si una operación de autenticación está en progreso.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * StateFlow que contiene mensajes de error relacionados con la autenticación.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Propiedad que indica si hay un usuario actualmente autenticado.
     */
    val isAuthenticated: Boolean
        get() = _user.value != null

    // ---- Funciones ----

    /**
     * Inicia sesión con el correo electrónico y contraseña proporcionados.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     */
    fun login(email: String, password: String) = executeAuth {
        handleAuthResult(authRepository.login(email, password))
    }




    /**
     * Registra un nuevo usuario con el correo electrónico, contraseña y nombre opcional.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @param name Nombre opcional del usuario.
     */
    fun register(email: String, password: String, name: String?) = executeAuth {
        handleAuthResult(authRepository.register(email, password)) { user ->
            saveUserInFirestore(user, name ?: user.displayName ?: "Usuario", email)
        }
    }

    /**
     * Envía un correo electrónico para restablecer la contraseña al correo proporcionado.
     *
     * @param email Correo electrónico al que se enviará el enlace de restablecimiento.
     * @param callback Función que se invoca al completar, indicando éxito o fallo.
     */
    fun sendPasswordResetEmail(email: String, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetEmail(email)
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.localizedMessage)
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        authRepository.logout()
        _user.value = null
    }

    /**
     * Maneja el resultado de Google Sign-In intercambiando el ID token por credenciales de Firebase.
     *
     * @param idToken El ID token obtenido de Google Sign-In.
     */
    fun handleGoogleSignInResult(idToken: String?) {
        if (idToken.isNullOrEmpty()) {
            showError("El token de Google es inválido o nulo.")
            return
        }

        // Log para depuración
        println("ID Token recibido: $idToken")

        executeAuth {
            handleAuthResult(authRepository.signInWithGoogle(idToken)) { user ->
                println("Usuario autenticado: ${user.uid}")
                saveUserInFirestore(
                    user = user,
                    name = user.displayName ?: "Usuario",
                    email = user.email ?: "Correo no disponible"
                )
            }
        }
    }

    /**
     * Guarda la información del usuario en Firestore.
     *
     * @param user El [FirebaseUser] autenticado.
     * @param name El nombre del usuario.
     * @param email El correo electrónico del usuario.
     */
    private suspend fun saveUserInFirestore(user: FirebaseUser, name: String, email: String) {
        try {
            val profileImage = user.photoUrl?.toString()
            authRepository.firestoreRepository.saveUser(user.uid, name, email, profileImage)
        } catch (e: Exception) {
            showError("Error al guardar el usuario: ${e.localizedMessage}")
        }
    }

    // ---- Configuración de Google Sign-In ----

    /**
     * Crea una instancia de [GetGoogleIdOption] para iniciar Google Sign-In.
     *
     * @param context El contexto de la aplicación.
     * @return Una instancia configurada de [GetGoogleIdOption].
     */
    fun getGoogleIdOption(context: Context): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true) // Cambiar a 'false' si deseas incluir cuentas no autorizadas
            .setServerClientId(context.getString(R.string.default_web_client_id)) // ID del cliente
            .setAutoSelectEnabled(true) // Habilitar selección automática para usuarios recurrentes
            .setNonce("secure_nonce_here") // Cambiar por un valor seguro y único por sesión
            .build()
    }

    /**
     * Obtiene un [GoogleSignInClient] configurado para la aplicación.
     *
     * @param activity La actividad actual.
     * @return Una instancia configurada de [GoogleSignInClient].
     */
    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        // Configurar GoogleSignInOptions
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Retornar el cliente de Google Sign-In configurado
        return GoogleSignIn.getClient(activity, googleSignInOptions)
    }

    /**
     * Inicia el flujo de Google Sign-In utilizando el lanzador proporcionado.
     *
     * @param launcher El [ManagedActivityResultLauncher] para iniciar el inicio de sesión.
     */
    fun launchGoogleSignIn(launcher: ManagedActivityResultLauncher<Unit, String?>) {
        launcher.launch(Unit) // Usar Unit como input
    }

    // ---- Manejo de Resultados ----

    /**
     * Maneja el resultado de una operación de autenticación.
     *
     * @param result El [AuthResult] de la operación de autenticación.
     * @param onSuccess Función opcional a ejecutar tras una autenticación exitosa.
     */
    private fun handleAuthResult(
        result: AuthResult,
        onSuccess: suspend (FirebaseUser) -> Unit = {}
    ) {
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

    /**
     * Ejecuta una operación de autenticación dentro de una corrutina, gestionando el estado de carga y errores.
     *
     * @param authOperation La función suspend que representa la operación de autenticación.
     */
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

    /**
     * Valida que las contraseñas proporcionadas coincidan.
     *
     * @param password La primera contraseña.
     * @param confirmPassword La contraseña de confirmación.
     * @return `true` si las contraseñas coinciden, `false` en caso contrario.
     */
    fun validatePasswords(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            true
        } else {
            showError("Las contraseñas no coinciden")
            false
        }
    }

    /**
     * Restablece cualquier mensaje de error que se esté mostrando.
     */
    fun resetError() {
        _errorMessage.value = null
    }

    /**
     * Establece un mensaje de error para mostrar al usuario.
     *
     * @param message El mensaje de error a mostrar.
     */
    fun showError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }
}
