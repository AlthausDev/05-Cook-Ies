package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Representa los posibles resultados de las operaciones relacionadas con usuarios.
 */
sealed class UserResult {
    /**
     * Indica que la operación fue exitosa.
     *
     * @param user El perfil del usuario obtenido o actualizado.
     */
    data class Success(val user: UserProfile) : UserResult()

    /**
     * Indica que la operación falló debido a una excepción.
     *
     * @param exception La excepción generada durante la operación.
     */
    data class Failure(val exception: Exception) : UserResult()

    /**
     * Indica que el usuario solicitado no fue encontrado.
     */
    object UserNotFound : UserResult()
}

/**
 * Repositorio que gestiona las operaciones relacionadas con usuarios en Firebase.
 *
 * @property firebaseAuth Proporciona autenticación de usuarios con Firebase.
 * @property firestore Proporciona acceso a la base de datos de Firestore.
 */
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Obtiene el usuario autenticado actualmente en Firebase Authentication.
     *
     * @return El usuario autenticado actualmente o `null` si no hay un usuario autenticado.
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Guarda o actualiza un perfil de usuario en Firestore.
     *
     * @param userProfile El perfil del usuario que se desea guardar o actualizar.
     * @return [UserResult.Success] si la operación fue exitosa,
     * [UserResult.UserNotFound] si el ID del usuario es `null`, o
     * [UserResult.Failure] si ocurre un error durante la operación.
     */
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

    /**
     * Obtiene el perfil de un usuario desde Firestore.
     *
     * @param userId El ID del usuario cuyo perfil se desea obtener.
     * @return [UserResult.Success] con el perfil del usuario si se encuentra,
     * [UserResult.UserNotFound] si el perfil no existe, o
     * [UserResult.Failure] si ocurre un error durante la operación.
     */
    suspend fun getUserProfile(userId: String): UserResult {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val userProfile = snapshot.toObject(UserProfile::class.java)
            userProfile?.let { UserResult.Success(it) } ?: UserResult.UserNotFound
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    /**
     * Actualiza el correo electrónico del usuario autenticado en Firebase Authentication.
     *
     * @param newEmail El nuevo correo electrónico que se desea establecer.
     * @return [UserResult.Success] si la operación fue exitosa,
     * [UserResult.UserNotFound] si no hay un usuario autenticado, o
     * [UserResult.Failure] si ocurre un error durante la operación.
     */
    suspend fun updateUserEmail(newEmail: String): UserResult {
        val user = firebaseAuth.currentUser ?: return UserResult.UserNotFound
        return try {
            user.updateEmail(newEmail).await()
            getUserProfile(user.uid)
        } catch (e: Exception) {
            UserResult.Failure(e)
        }
    }

    /**
     * Actualiza la foto de perfil de un usuario en Firestore.
     *
     * @param userId El ID del usuario cuyo perfil se desea actualizar.
     * @param imageUri El URI de la nueva imagen de perfil.
     * @return [UserResult.Success] si la operación fue exitosa,
     * [UserResult.UserNotFound] si el usuario no existe, o
     * [UserResult.Failure] si ocurre un error durante la operación.
     */
    suspend fun updateUserProfileImage(userId: String, imageUri: String): UserResult {
        val userProfileResult = getUserProfile(userId)
        if (userProfileResult is UserResult.Success) {
            val updatedProfile = userProfileResult.user.copy(profileImage = imageUri)
            return saveUserProfile(updatedProfile)
        }
        return UserResult.UserNotFound
    }
}
