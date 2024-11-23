package com.althaus.dev.cookIes.data.repository

import android.app.Activity
import android.content.Context
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Resultado de autenticación
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Failure(val exception: Exception) : AuthResult()
    object UserNotFound : AuthResult()
}

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    val firestoreRepository: FirestoreRepository,
    @ApplicationContext private val context: Context
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Google Sign-In Client configurado internamente
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // Debe ser correcto
        .requestEmail()
        .build()


    // Llamada segura para autenticación
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

    // Métodos de Autenticación
    suspend fun login(email: String, password: String): AuthResult = safeAuthCall {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()?.user
    }

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

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }


    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.let {
                // Log para verificar si el usuario se obtiene
                println("FirebaseAuth: Usuario autenticado: ${it.uid}")
                AuthResult.Success(it)
            } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            println("Error en signInWithGoogle: ${e.localizedMessage}")
            AuthResult.Failure(e)
        }
    }



    private suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return safeAuthCall {
            firebaseAuth.signInWithCredential(credential).await()?.user
        }
    }

    suspend fun updateUserEmail(newEmail: String, currentPassword: String): AuthResult {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
                ?: return AuthResult.UserNotFound // Usuario no autenticado
            val email = user.email ?: return AuthResult.Failure(Exception("Correo no encontrado"))

            // Re-autenticar al usuario antes de cambiar el correo
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await() // Re-autenticación obligatoria

            // Actualizar el correo
            user.updateEmail(newEmail).await()
            AuthResult.Success(user) // Retornar éxito con el usuario actualizado
        } catch (e: Exception) {
            AuthResult.Failure(e) // Manejar excepciones
        }
    }


    // Métodos de Actualización
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

    suspend fun updateUserEmail(newEmail: String): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            currentUser.updateEmail(newEmail).await()
            currentUser
        }
    }

    suspend fun updateUserPassword(newPassword: String, currentPassword: String): AuthResult {
        val reAuthResult = reAuthenticate(currentPassword)
        if (reAuthResult is AuthResult.Failure) return reAuthResult

        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            currentUser.updatePassword(newPassword).await()
            currentUser
        }
    }

    suspend fun reAuthenticate(password: String): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        val email = currentUser.email ?: return AuthResult.Failure(Exception("Correo no asociado."))
        val credential = EmailAuthProvider.getCredential(email, password)
        return safeAuthCall {
            currentUser.reauthenticate(credential).await()
            currentUser
        }
    }

    // Cerrar sesión
    fun logout() {
        firebaseAuth.signOut()
       //googleSignInClient.signOut()
    }
}
