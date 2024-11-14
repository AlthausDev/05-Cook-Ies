package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",                  // ID del usuario en Firestore
    val name: String = "",                            // Nombre del usuario, predeterminado a una cadena vacía
    val email: String = "",                           // Email, predeterminado a una cadena vacía para evitar nulls
    val profileImageUrl: String? = null,              // URL de la foto de perfil; nulo si no tiene una imagen
    val favorites: List<String> = emptyList(),        // Lista inmutable de IDs de recetas favoritas
    val bio: String = "",                             // Biografía del usuario
    val creationDate: Long = System.currentTimeMillis(), // Timestamp de creación de la cuenta
    val isVerified: Boolean = false                   // Indicador de cuenta verificada
)
