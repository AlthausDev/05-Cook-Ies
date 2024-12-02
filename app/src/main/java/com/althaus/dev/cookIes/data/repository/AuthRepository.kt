package com.althaus.dev.cookIes.data.repository

import android.content.Context
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Clase sellada que representa los posibles resultados de las operaciones de autenticación.
 */
sealed class AuthResult {
    /**
     * Indica que la operación de autenticación fue exitosa.
     *
     * @param user Usuario autenticado.
     */
    data class Success(val user: FirebaseUser) : AuthResult()

    /**
     * Indica que la operación de autenticación falló.
     *
     * @param exception Excepción que describe el error ocurrido.
     */
    data class Failure(val exception: Exception) : AuthResult()

    /**
     * Indica que el usuario no fue encontrado.
     */
    object UserNotFound : AuthResult()
}


/**
 * Repositorio para gestionar la autenticación y las operaciones relacionadas con usuarios en Firebase.
 *
 * Este repositorio utiliza [FirebaseAuth] para realizar operaciones de autenticación
 * como inicio de sesión, registro, actualización de datos del usuario y más. También
 * integra Google Sign-In y otras funcionalidades relacionadas con la autenticación.
 */
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    val firestoreRepository: FirestoreRepository,
    @ApplicationContext private val context: Context
) {

    /**
     * Usuario actualmente autenticado.
     *
     * @return El usuario autenticado ([FirebaseUser]) o `null` si no hay usuario activo.
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Configuración de Google Sign-In Options.
     */
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    // ---- Métodos de autenticación ----

    /**
     * Inicia sesión con correo y contraseña.
     *
     * @param email Correo del usuario.
     * @param password Contraseña del usuario.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun login(email: String, password: String): AuthResult = safeAuthCall {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()?.user
    }

    /**
     * Registra un nuevo usuario con correo y contraseña.
     *
     * También guarda el perfil del usuario en Firestore mediante [FirestoreRepository].
     *
     * @param email Correo del usuario.
     * @param password Contraseña del usuario.
     * @param name Nombre del usuario (opcional).
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun register(email: String, password: String, name: String? = null): AuthResult {
        return safeAuthCall {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result?.user
            user?.let {
                val displayName = name ?: it.displayName ?: "Usuario"
                firestoreRepository.saveUser(it.uid, displayName, email, null)
            }
            user
        }
    }

    /**
     * Envía un correo para restablecer la contraseña.
     *
     * @param email Correo del usuario.
     * @throws Exception Si ocurre algún error durante la operación.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /**
     * Inicia sesión con Google utilizando un token de ID.
     *
     * @param idToken Token de ID proporcionado por Google.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    /**
     * Actualiza el correo electrónico del usuario actual.
     *
     * Requiere reautenticación antes de cambiar el correo.
     *
     * @param newEmail Nuevo correo del usuario.
     * @param currentPassword Contraseña actual para reautenticación.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun updateUserEmail(newEmail: String, currentPassword: String): AuthResult {
        return try {
            val user = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
            val email = user.email ?: return AuthResult.Failure(Exception("Correo no encontrado"))

            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updateEmail(newEmail).await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    /**
     * Actualiza el nombre del usuario actual.
     *
     * @param newName Nuevo nombre del usuario.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun updateUserName(newName: String): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()
            currentUser.updateProfile(profileUpdates).await()
            currentUser
        }
    }

    /**
     * Actualiza la contraseña del usuario actual.
     *
     * Requiere reautenticación antes de cambiar la contraseña.
     *
     * @param newPassword Nueva contraseña.
     * @param currentPassword Contraseña actual para reautenticación.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun updateUserPassword(newPassword: String, currentPassword: String): AuthResult {
        val reAuthResult = reAuthenticate(currentPassword)
        if (reAuthResult is AuthResult.Failure) return reAuthResult

        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            currentUser.updatePassword(newPassword).await()
            currentUser
        }
    }

    /**
     * Reautentica al usuario actual con la contraseña proporcionada.
     *
     * @param password Contraseña actual del usuario.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    suspend fun reAuthenticate(password: String): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        val email = currentUser.email ?: return AuthResult.Failure(Exception("Correo no asociado."))
        val credential = EmailAuthProvider.getCredential(email, password)
        return safeAuthCall {
            currentUser.reauthenticate(credential).await()
            currentUser
        }
    }

    /**
     * Cierra sesión del usuario actual.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    // ---- Métodos auxiliares ----

    /**
     * Llamada segura para manejar operaciones de autenticación con excepciones comunes.
     *
     * @param authCall Operación de autenticación a ejecutar.
     * @return [AuthResult] que indica el resultado de la operación.
     */
    private suspend fun safeAuthCall(authCall: suspend () -> FirebaseUser?): AuthResult {
        return try {
            authCall()?.let { AuthResult.Success(it) } ?: AuthResult.UserNotFound
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Failure(Exception("Credenciales inválidas."))
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Failure(Exception("Usuario no encontrado."))
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }
}
