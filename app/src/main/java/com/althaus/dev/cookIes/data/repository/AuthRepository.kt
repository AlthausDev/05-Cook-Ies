// data/repository/AuthRepository.kt
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

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    // Propiedad para obtener el usuario actual
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Función para iniciar sesión con email y contraseña
    suspend fun login(email: String, password: String): FirebaseUser? {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await().user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Función para iniciar sesión de forma anónima
    suspend fun loginAnonymously(): FirebaseUser? {
        return try {
            firebaseAuth.signInAnonymously().await().user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Función para registrar un usuario con email y contraseña
    suspend fun register(email: String, password: String): FirebaseUser? {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Función para verificar si hay un usuario logueado
    fun isUserLogged(): Boolean = currentUser != null

    // Función para cerrar sesión
    fun logout() {
        firebaseAuth.signOut()
    }

    // Verificación de código (por ejemplo, para autenticación con teléfono)
    suspend fun verifyCode(verificationCode: String, phoneCode: String): FirebaseUser? {
        val credentials = PhoneAuthProvider.getCredential(verificationCode, phoneCode)
        return signInWithCredential(credentials)
    }

    // Función privada para iniciar sesión con una credencial
    private suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser? {
        return try {
            firebaseAuth.signInWithCredential(credential).await().user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Función para obtener el cliente de Google para la autenticación con Google
    fun getGoogleClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Función para iniciar sesión con Google usando el idToken
    suspend fun loginWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    // Función para iniciar registro con un proveedor externo
//    suspend fun initRegisterWithProvider(
//        activity: Activity, provider: OAuthProvider
//    ): FirebaseUser? {
//        return firebaseAuth.pendingAuthResult?.user ?: try {
//            firebaseAuth.startActivityForSignInWithProvider(activity, provider).await().user
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
}
