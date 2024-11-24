package com.althaus.dev.cookIes.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Notification(
    @DocumentId val id: String = "", // Identificador único del documento en Firestore
    val title: String = "", // Campo 'title' en Firestore
    val message: String = "", // Campo 'message' en Firestore
    val type: String = "GENERAL", // Campo 'type' en Firestore como String
    val timestamp: Long = System.currentTimeMillis(), // Campo 'timestamp' en Firestore (milisegundos)
    val read: Boolean = false, // Campo 'read' en Firestore, booleano
    val recipientId: String = "", // Campo 'recipientId' en Firestore
    val relatedRecipeId: String? = null, // Campo 'relatedRecipeId' en Firestore
    val readableTimestamp: String? = null // Campo 'readableTimestamp' en Firestore (opcional)
) : Parcelable {

    /**
     * Marca esta notificación como leída.
     * Retorna una nueva instancia de la notificación con el campo `read` establecido como `true`.
     */
    fun markAsRead(): Notification {
        return this.copy(read = true)
    }

    /**
     * Convierte esta notificación a un mapa para ser almacenado en Firestore.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "message" to message,
            "type" to type,
            "timestamp" to timestamp,
            "read" to read,
            "recipientId" to recipientId,
            "relatedRecipeId" to relatedRecipeId,
            "readableTimestamp" to readableTimestamp
        )
    }

    companion object {
        /**
         * Crea una instancia de `Notification` a partir de un mapa (usado para convertir datos de Firestore).
         */
        fun fromMap(map: Map<String, Any?>): Notification {
            return Notification(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                message = map["message"] as? String ?: "",
                type = map["type"] as? String ?: "GENERAL",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                read = map["read"] as? Boolean ?: false,
                recipientId = map["recipientId"] as? String ?: "",
                relatedRecipeId = map["relatedRecipeId"] as? String,
                readableTimestamp = map["readableTimestamp"] as? String
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
    GENERAL("Notificación general")
}