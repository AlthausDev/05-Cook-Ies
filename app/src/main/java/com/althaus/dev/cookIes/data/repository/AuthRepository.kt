package com.althaus.dev.cookIes.data.repository

import android.app.Activity
import android.content.Context
import com.althaus.dev.cookIes.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Failure(val exception: Exception) : AuthResult()
    object UserNotFound : AuthResult()
}

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Iniciar sesión con email y contraseña
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.UserNotFound
            }
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Iniciar sesión anónimo
    suspend fun loginAnonymously(): AuthResult {
        return try {
            val user = firebaseAuth.signInAnonymously().await().user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.UserNotFound
            }
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Registro de usuario
    suspend fun register(email: String, password: String): AuthResult {
        return try {
            val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.UserNotFound
            }
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Verificar si hay usuario autenticado
    fun isUserLogged(): Boolean = currentUser != null

    // Cerrar sesión
    fun logout() {
        firebaseAuth.signOut()
    }

    // Verificar código de autenticación (ej. para autenticación por teléfono)
    suspend fun verifyCode(verificationCode: String, phoneCode: String): AuthResult {
        val credentials = PhoneAuthProvider.getCredential(verificationCode, phoneCode)
        return signInWithCredential(credentials)
    }

    // Iniciar sesión con una credencial
    private suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return try {
            val user = firebaseAuth.signInWithCredential(credential).await().user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.UserNotFound
            }
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Cliente de Google para autenticación con Google
    fun getGoogleClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Iniciar sesión con Google usando el idToken
    suspend fun loginWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    // Registro con proveedor externo (OAuth)
    suspend fun initRegisterWithProvider(
        activity: Activity, provider: OAuthProvider
    ): AuthResult {
        return try {
            val user = firebaseAuth.pendingAuthResult?.user
                ?: firebaseAuth.startActivityForSignInWithProvider(activity, provider).await().user
            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.UserNotFound
            }
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }
}
