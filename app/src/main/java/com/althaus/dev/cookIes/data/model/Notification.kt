package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Notification(
    @DocumentId val id: String = "",          // ID único de la notificación en Firestore
    val title: String,                        // Título de la notificación
    val message: String,                      // Mensaje de la notificación con detalles adicionales
    val type: NotificationType = NotificationType.GENERAL, // Tipo de notificación
    val timestamp: Long = System.currentTimeMillis(), // Marca de tiempo de la notificación
    val isRead: Boolean = false,              // Indicador de si el usuario ya leyó la notificación
    val recipientId: String,                  // ID del usuario que recibe la notificación
    val relatedRecipeId: String? = null       // ID de una receta relacionada, si aplica
)

// Enum para representar los tipos de notificación
enum class NotificationType {
    NEW_RECIPE,      // Notificación de nueva receta
    COMMENT,         // Notificación de comentario
    FAVORITE,        // Notificación de receta marcada como favorita
    REMINDER,        // Recordatorio para el usuario
    GENERAL          // Tipo general para otras notificaciones
}