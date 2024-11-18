package com.althaus.dev.cookIes.data.model

import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*


@IgnoreExtraProperties
data class Notification(
    @DocumentId val id: String = "",
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val recipientId: String,
    val relatedRecipeId: String? = null
) {
    init {
        require(title.isNotBlank()) { "El título de la notificación no puede estar vacío." }
        require(recipientId.isNotBlank()) { "El ID del destinatario no puede estar vacío." }

        if (type == NotificationType.NEW_RECIPE || type == NotificationType.FAVORITE) {
            require(!relatedRecipeId.isNullOrEmpty()) {
                "relatedRecipeId es obligatorio para notificaciones de tipo $type."
            }
        }
    }

    fun markAsRead(): Notification {
        return this.copy(isRead = true)
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "title" to title,
            "message" to message,
            "type" to type.name,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "recipientId" to recipientId
        )
        relatedRecipeId?.let {
            map["relatedRecipeId"] = it
        }
        return map
    }

    fun getReadableTimestamp(): String {
        val date = Date(this.timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }


    suspend fun saveToFirestore(repository: FirestoreRepository) {
        try {
            val notificationId = if (id.isBlank()) repository.generateNewId("notifications") else id
            repository.saveNotification(notificationId, toMap())
        } catch (e: Exception) {
            throw Exception("Error al guardar la notificación en Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun updateInFirestore(repository: FirestoreRepository, updates: Map<String, Any>) {
        try {
            if (id.isBlank()) throw IllegalArgumentException("No se puede actualizar una notificación sin ID.")
            repository.updateNotification(id, updates)
        } catch (e: Exception) {
            throw Exception("Error al actualizar la notificación en Firestore: ${e.localizedMessage}")
        }
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
    NEW_RECIPE,
    COMMENT,
    FAVORITE,
    REMINDER,
    GENERAL
}
