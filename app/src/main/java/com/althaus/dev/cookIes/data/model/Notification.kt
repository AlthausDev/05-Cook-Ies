package com.althaus.dev.cookIes.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

/**
 * Representa una notificación en el sistema.
 *
 * Esta clase modela las propiedades y comportamientos asociados a una notificación,
 * incluyendo detalles como el título, mensaje, tipo, marca temporal y el estado de lectura.
 * También permite la conversión de y hacia estructuras compatibles con Firestore.
 */
@Parcelize
@IgnoreExtraProperties
data class Notification(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val recipientId: String = "",
    val relatedRecipeId: String? = null,
    val readableTimestamp: String? = null
) : Parcelable {

    /**
     * Marca esta notificación como leída.
     *
     * @return Una nueva instancia de `Notification` con el campo `read` establecido como `true`.
     */
    fun markAsRead(): Notification {
        return this.copy(read = true)
    }

    /**
     * Convierte esta notificación a un mapa para ser almacenado en Firestore.
     *
     * @return Un mapa con las propiedades de la notificación.
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
         * Crea una instancia de `Notification` a partir de un mapa.
         *
         * @param map Mapa que contiene las propiedades de la notificación.
         * @return Una instancia de `Notification` basada en los valores del mapa.
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

/**
 * Tipos de notificación disponibles en el sistema.
 *
 * Representa los posibles tipos de notificaciones que un usuario puede recibir,
 * proporcionando una descripción legible para cada uno.
 */
@Parcelize
enum class NotificationType(val description: String) : Parcelable {
    /**
     * Nueva receta disponible.
     */
    NEW_RECIPE("Nueva receta"),

    /**
     * Nuevo comentario en una receta.
     */
    COMMENT("Nuevo comentario"),

    /**
     * Receta agregada a favoritos.
     */
    FAVORITE("Agregado a favoritos"),

    /**
     * Recordatorio.
     */
    REMINDER("Recordatorio"),

    /**
     * Notificación general.
     */
    GENERAL("Notificación general")
}
