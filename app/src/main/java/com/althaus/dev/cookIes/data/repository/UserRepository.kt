package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class UserResult {
    data class Success(val user: UserProfile) : UserResult()
    data class Failure(val exception: Exception) : UserResult()
    object UserNotFound : UserResult()
}

class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Obtener el usuario actual
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // Guardar o actualizar un perfil de usuario en Firestore
    suspend fun saveUserProfile(userProfile: UserProfile): UserResult {
        return try {
            firestore.collection("users")
                .document(userProfile.id ?: return UserResult.UserNotFound)
                .set(userProfile)
                .await()
            UserResult.Success(userProfile)
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    // Obtener perfil de usuario desde Firestore
    suspend fun getUserProfile(userId: String): UserResult {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            userProfile?.let { UserResult.Success(it) } ?: UserResult.UserNotFound
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    // Actualizar email del usuario
    suspend fun updateUserEmail(newEmail: String): UserResult {
        val user = firebaseAuth.currentUser ?: return UserResult.UserNotFound
        return try {
            user.updateEmail(newEmail).await()
            val updatedUserProfile = getUserProfile(user.uid)
            updatedUserProfile
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    // Actualizar foto de perfil del usuario
    suspend fun updateUserProfileImage(userId: String, imageUri: String): UserResult {
        val userProfileResult = getUserProfile(userId)
        if (userProfileResult is UserResult.Success) {
            val updatedProfile = userProfileResult.user.copy(profileImage = imageUri)
            return saveUserProfile(updatedProfile)
        }
        return UserResult.UserNotFound
    }
}
