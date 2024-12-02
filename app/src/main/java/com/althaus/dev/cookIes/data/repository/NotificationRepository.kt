package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Repositorio para manejar operaciones relacionadas con notificaciones en Firestore.
 *
 * Este repositorio proporciona métodos para obtener notificaciones, marcarlas como leídas y eliminarlas.
 *
 * @property firestore Instancia de [FirebaseFirestore] utilizada para interactuar con la base de datos.
 */
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val notificationsCollection = firestore.collection("notifications")

    /**
     * Obtiene todas las notificaciones como un flujo ([Flow]).
     *
     * @return Un flujo que emite resultados de tipo [NotificationResult] con una lista de [Notification].
     */
    suspend fun getNotifications(): Flow<NotificationResult<List<Notification>>> = flow {
        try {
            // Consulta a Firestore para obtener las notificaciones
            val snapshot = notificationsCollection.get().await()

            // Mapeo de los documentos a objetos Notification
            val notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }

            // Emitir las notificaciones como resultado exitoso
            emit(NotificationResult.Success(notifications))
        } catch (e: Exception) {
            // Emitir un resultado de falla en caso de error
            emit(NotificationResult.Failure(e))
        }
    }

    /**
     * Marca una notificación como leída en Firestore.
     *
     * @param notificationId ID de la notificación que se desea marcar como leída.
     * @return `true` si la operación fue exitosa, `false` en caso contrario.
     */
    suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            // Actualizar el campo "isRead" a true
            notificationsCollection.document(notificationId).update("isRead", true).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Elimina una notificación de Firestore.
     *
     * @param notificationId ID de la notificación que se desea eliminar.
     * @return `true` si la operación fue exitosa, `false` en caso contrario.
     */
    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            // Eliminar el documento de la colección "notifications"
            notificationsCollection.document(notificationId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Resultado de las operaciones relacionadas con notificaciones.
 *
 * Esta clase sellada representa los posibles resultados de una operación:
 * - [Success]: La operación se completó correctamente con los datos asociados.
 * - [Failure]: La operación falló con una excepción asociada.
 */
sealed class NotificationResult<out T> {
    /**
     * Representa un resultado exitoso.
     *
     * @param data Los datos asociados al resultado exitoso.
     */
    data class Success<out T>(val data: T) : NotificationResult<T>()

    /**
     * Representa un resultado fallido.
     *
     * @param exception La excepción asociada al fallo.
     */
    data class Failure(val exception: Exception) : NotificationResult<Nothing>()
}
