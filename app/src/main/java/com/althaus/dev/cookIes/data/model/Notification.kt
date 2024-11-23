package com.althaus.dev.cookIes.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
@IgnoreExtraProperties
data class Notification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val recipientId: String = "",
    val relatedRecipeId: String? = null
) : Parcelable {
    init {
        require(title.isNotBlank()) { "El título de la notificación no puede estar vacío." }
        require(recipientId.isNotBlank()) { "El ID del destinatario no puede estar vacío." }

        if (type in listOf(NotificationType.NEW_RECIPE, NotificationType.FAVORITE)) {
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
        if (type in listOf(NotificationType.NEW_RECIPE, NotificationType.FAVORITE)) {
            relatedRecipeId?.let { map["relatedRecipeId"] = it }
        }
        return map
    }

    fun getReadableTimestamp(): String {
        val date = Date(this.timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Notification {
            return Notification(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                message = map["message"] as? String ?: "",
                type = try {
                    NotificationType.valueOf(map["type"] as? String ?: "GENERAL")
                } catch (e: IllegalArgumentException) {
                    NotificationType.GENERAL
                },
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isRead = map["isRead"] as? Boolean ?: false,
                recipientId = map["recipientId"] as? String ?: "",
                relatedRecipeId = map["relatedRecipeId"] as? String
            )
        }
    }
}

@Parcelize
enum class NotificationType(val description: String) : Parcelable {
    NEW_RECIPE("Nueva receta"),
    COMMENT("Nuevo comentario"),
    FAVORITE("Agregado a favoritos"),
    REMINDER("Recordatorio"),
    GENERAL("Notificación general");
}
