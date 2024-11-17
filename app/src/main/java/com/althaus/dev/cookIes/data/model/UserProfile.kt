package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",               // ID único del usuario en Firestore
    val name: String = "",                         // Nombre del usuario
    val email: String = "",                        // Correo electrónico del usuario
    val profileImage: String? = null,              // URL de la imagen de perfil (opcional)
    val favorites: List<String> = emptyList(),     // IDs de recetas favoritas
    val bio: String = "",                          // Biografía del usuario
    val creationDate: Timestamp = Timestamp.now(), // Fecha de creación
    val isVerified: Boolean = false                // Si la cuenta está verificada
) {
    // Conversión a Map para Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "profileImage" to profileImage,
            "favorites" to favorites,
            "bio" to bio,
            "creationDate" to creationDate,
            "isVerified" to isVerified
        )
    }

    companion object {
        // Conversión desde Map de Firestore
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                profileImage = map["profileImage"] as? String,
                favorites = map["favorites"] as? List<String> ?: emptyList(),
                bio = map["bio"] as? String ?: "",
                creationDate = map["creationDate"] as? Timestamp ?: Timestamp.now(),
                isVerified = map["isVerified"] as? Boolean ?: false
            )
        }
    }

    // Métodos adicionales para facilitar el manejo
    fun addFavorite(recipeId: String): UserProfile {
        if (recipeId.isNotBlank() && !favorites.contains(recipeId)) {
            val updatedFavorites = favorites + recipeId
            return this.copy(favorites = updatedFavorites)
        }
        return this
    }

    fun removeFavorite(recipeId: String): UserProfile {
        if (favorites.contains(recipeId)) {
            val updatedFavorites = favorites - recipeId
            return this.copy(favorites = updatedFavorites)
        }
        return this
    }
}
