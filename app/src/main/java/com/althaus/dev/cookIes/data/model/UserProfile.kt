package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.Timestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val favorites: List<String> = emptyList(),
    val bio: String = "",
    val creationDate: Timestamp = Timestamp.now(),
    val isVerified: Boolean = false
) {
    // Conversión a Map
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
        // Conversión desde Map
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
}
