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
    googleSignInClient: GoogleSignInClient,
    @ApplicationContext private val context: Context
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Singleton de GoogleSignInClient
    private val googleSignInClientInstance: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build())
    }

    // Obtener GoogleSignInClient
    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClientInstance

    // Método auxiliar para manejar errores de autenticación de manera segura
    private suspend fun safeAuthCall(authCall: suspend () -> FirebaseUser?): AuthResult {
        return try {
            authCall()?.let { AuthResult.Success(it) } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // Iniciar sesión con Google usando el ID Token
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    // Iniciar sesión con credencial de autenticación
    private suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return safeAuthCall {
            firebaseAuth.signInWithCredential(credential).await()?.user
        }
    }

    // Iniciar sesión con email y contraseña
    suspend fun login(email: String, password: String): AuthResult = safeAuthCall {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()?.user
    }

    // Registrar usuario con email y contraseña
    suspend fun register(email: String, password: String): AuthResult = safeAuthCall {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()?.user
    }

    // Actualizar el perfil de usuario con nombre y foto
    suspend fun updateUserProfile(userProfile: UserProfile): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userProfile.name)
                .setPhotoUri(userProfile.profileImage?.let { Uri.parse(it.toString()) })
                .build()
            currentUser.updateProfile(profileUpdates).await()

            // Si el email ha cambiado, actualiza también el email del usuario
            if (userProfile.email != null && userProfile.email != currentUser.email) {
                currentUser.updateEmail(userProfile.email).await()
            }
            currentUser
        }
    }

    // Registro con proveedor externo (OAuth)
    suspend fun initRegisterWithProvider(activity: Activity, provider: OAuthProvider): AuthResult {
        return safeAuthCall {
            firebaseAuth.startActivityForSignInWithProvider(activity, provider).await()?.user
        }
    }

    // Cerrar sesión del usuario
    fun logout() {
        firebaseAuth.signOut()
    }
}
