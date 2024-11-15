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

    // Obtener notificaciones como Flow
    suspend fun getNotifications(): Flow<NotificationResult<List<Notification>>> = flow {
        try {
            val snapshot = notificationsCollection.get().await()
            val notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
            emit(NotificationResult.Success(notifications))
        } catch (e: Exception) {
            emit(NotificationResult.Failure(e))
        }
    }

    // Marcar notificación como leída
    suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId).update("isRead", true).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Eliminar notificación
    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
