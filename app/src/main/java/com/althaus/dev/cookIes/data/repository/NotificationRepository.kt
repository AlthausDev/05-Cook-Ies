package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class NotificationResult<out T> {
    data class Success<out T>(val data: T) : NotificationResult<T>()
    data class Failure(val exception: Exception) : NotificationResult<Nothing>()
}

class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val notificationsCollection = firestore.collection("notifications")

    // Obtener todas las notificaciones para un usuario específico
    suspend fun getUserNotifications(userId: String): Flow<NotificationResult<List<Notification>>> = flow {
        emit(safeNotificationCall {
            val snapshot = notificationsCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
        })
    }

    // Marcar una notificación como leída
    suspend fun markNotificationAsRead(notificationId: String): NotificationResult<Boolean> = safeNotificationCall {
        notificationsCollection.document(notificationId).update("isRead", true).await()
        true
    }

    // Agregar una nueva notificación
    suspend fun addNotification(notification: Notification): NotificationResult<Boolean> = safeNotificationCall {
        notificationsCollection.add(notification).await()
        true
    }

    // Método auxiliar para manejar errores en operaciones con Firestore
    private suspend fun <T> safeNotificationCall(call: suspend () -> T): NotificationResult<T> {
        return try {
            NotificationResult.Success(call())
        } catch (e: Exception) {
            NotificationResult.Failure(e)
        }
    }
}
