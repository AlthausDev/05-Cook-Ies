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
    private val firestoreRepository: FirestoreRepository,
    private val googleSignInClient: GoogleSignInClient,
    @ApplicationContext private val context: Context
){

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    private val googleSignInClientInstance: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClientInstance

    private suspend fun safeAuthCall(authCall: suspend () -> FirebaseUser?): AuthResult {
        return try {
            authCall()?.let { AuthResult.Success(it) } ?: AuthResult.UserNotFound
        } catch (e: Exception) {
            AuthResult.Failure(e)
        }
    }

    // ---- Métodos de Actualización Individual ----

    // Actualizar Nombre
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

    // Re-autenticar al usuario antes de realizar cambios sensibles
    suspend fun reAuthenticate(password: String): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        val email = currentUser.email ?: return AuthResult.Failure(Exception("El usuario no tiene un correo asociado."))
        val credential = EmailAuthProvider.getCredential(email, password)
        return safeAuthCall {
            currentUser.reauthenticate(credential).await()
            currentUser
        }
    }

    // Actualizar Correo Electrónico (con re-autenticación)
    suspend fun updateUserEmail(newEmail: String): AuthResult {
        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            currentUser.updateEmail(newEmail).await() // Cambia directamente el correo
            currentUser
        }
    }


    // Actualizar Contraseña (con re-autenticación)
    suspend fun updateUserPassword(newPassword: String, currentPassword: String): AuthResult {
        val reAuthResult = reAuthenticate(currentPassword)
        if (reAuthResult is AuthResult.Failure) return reAuthResult

        val currentUser = firebaseAuth.currentUser ?: return AuthResult.UserNotFound
        return safeAuthCall {
            currentUser.updatePassword(newPassword).await()
            currentUser
        }
    }

    // ---- Métodos de Inicio de Sesión ----

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return signInWithCredential(credential)
    }

    private suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return safeAuthCall {
            firebaseAuth.signInWithCredential(credential).await()?.user
        }
    }

    suspend fun login(email: String, password: String): AuthResult = safeAuthCall {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()?.user
    }

    suspend fun register(email: String, password: String): AuthResult = safeAuthCall {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()?.user
    }

    suspend fun register(email: String, password: String, name: String): AuthResult {
        return safeAuthCall {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result?.user
            user?.let {
                val userProfile = UserProfile(
                    id = it.uid,
                    name = name,
                    email = email,
                    profileImage = null
                )
                firestoreRepository.saveUser(it.uid, name, email, null)
            }
            user
        }
    }


    // Registro con proveedor externo
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
