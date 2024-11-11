package com.althaus.dev.cookIes.data.repository

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
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

    // Inicio de sesión con credencial
    private suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return try {
            firebaseAuth.signInWithCredential(credential).await()?.let {
                AuthResult.Success(it.user!!)
            } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Obtener cliente de Google
    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Iniciar sesión con Google
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    // Iniciar sesión con email y contraseña
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()?.let {
                AuthResult.Success(it.user!!)
            } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Registro con email y contraseña
    suspend fun register(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()?.let {
                AuthResult.Success(it.user!!)
            } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Iniciar sesión anónima
    suspend fun loginAnonymously(): AuthResult {
        return try {
            firebaseAuth.signInAnonymously().await()?.let {
                AuthResult.Success(it.user!!)
            } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Actualizar perfil de usuario
    suspend fun updateUserProfile(userProfile: UserProfile): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.name)
                .setPhotoUri(userProfile.profileImage?.let { Uri.parse(it) })
                .build()
            currentUser.updateProfile(profileUpdates).await()

            if (userProfile.email != null && userProfile.email != currentUser.email) {
                currentUser.updateEmail(userProfile.email).await()
            }
            AuthResult.Success(currentUser)
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Registro con proveedor externo (OAuth)
    suspend fun initRegisterWithProvider(activity: Activity, provider: OAuthProvider): AuthResult {
        return try {
            // Inicia la actividad de inicio de sesión con el proveedor dado y espera el resultado
            val authResult = firebaseAuth.startActivityForSignInWithProvider(activity, provider).await()
            val user = authResult.user // Aquí obtenemos al usuario directamente del resultado de autenticación
            user?.let { AuthResult.Success(it) } ?: AuthResult.UserNotFound
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
}
