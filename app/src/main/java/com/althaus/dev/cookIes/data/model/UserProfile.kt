package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.Timestamp

/**
 * Representa el perfil de un usuario en el sistema.
 *
 * Modela las propiedades y comportamientos asociados al perfil del usuario, incluyendo
 * su nombre, correo, imagen de perfil, recetas favoritas, calificaciones y otros detalles.
 */
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val favorites: List<String> = emptyList(),
    val ratings: Map<String, Double> = emptyMap(),
    val bio: String = "",
    val creationDate: Timestamp = Timestamp.now(),
    val isVerified: Boolean = false
) {
    /**
     * Convierte este perfil de usuario a un mapa compatible con Firestore.
     *
     * @return Un mapa con las propiedades del perfil del usuario.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "profileImage" to profileImage,
            "favorites" to favorites,
            "ratings" to ratings,
            "bio" to bio,
            "creationDate" to creationDate,
            "isVerified" to isVerified
        )
    }

    companion object {
        /**
         * Crea una instancia de `UserProfile` a partir de un mapa.
         *
         * @param map Mapa que contiene las propiedades del perfil de usuario.
         * @return Una instancia de `UserProfile` basada en los valores del mapa.
         */
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                profileImage = map["profileImage"] as? String,
                favorites = map["favorites"] as? List<String> ?: emptyList(),
                ratings = map["ratings"] as? Map<String, Double> ?: emptyMap(),
                bio = map["bio"] as? String ?: "",
                creationDate = map["creationDate"] as? Timestamp ?: Timestamp.now(),
                isVerified = map["isVerified"] as? Boolean ?: false
            )
        }
    }
}
