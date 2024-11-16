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
) {
    init {
        if (type == NotificationType.NEW_RECIPE || type == NotificationType.FAVORITE) {
            require(!relatedRecipeId.isNullOrEmpty()) {
                "relatedRecipeId es obligatorio para notificaciones de tipo $type."
            }
        }
    }

    fun markAsRead(): Notification {
        return this.copy(isRead = true)
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "message" to message,
            "type" to type.name,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "recipientId" to recipientId,
            "relatedRecipeId" to relatedRecipeId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Notification {
            return Notification(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                message = map["message"] as? String ?: "",
                type = NotificationType.valueOf(map["type"] as? String ?: "GENERAL"),
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isRead = map["isRead"] as? Boolean ?: false,
                recipientId = map["recipientId"] as? String ?: "",
                relatedRecipeId = map["relatedRecipeId"] as? String
            )
        }
    }
}

enum class NotificationType {
    NEW_RECIPE,      // Notificación de nueva receta
    COMMENT,         // Notificación de comentario
    FAVORITE,        // Notificación de receta marcada como favorita
    REMINDER,        // Recordatorio para el usuario
    GENERAL          // Tipo general para otras notificaciones
}
