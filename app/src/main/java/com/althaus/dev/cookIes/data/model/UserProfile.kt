package com.althaus.dev.cookIes.data.model

data class UserProfile(
    val id: String,
    val name: String?,
    val email: String?,
    val profileImage: String? // Esto será una URL a la imagen de perfil
)
