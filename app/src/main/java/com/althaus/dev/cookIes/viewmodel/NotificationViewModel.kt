package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.repository.NotificationRepository
import com.althaus.dev.cookIes.data.repository.NotificationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // ---- Estados ----
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchNotifications()
    }

    // ---- Gestión de Notificaciones ----

    /**
     * Obtener todas las notificaciones del repositorio.
     */
    fun fetchNotifications() {
        executeWithLoading {
            try {
                notificationRepository.getNotifications().collect { result ->
                    when (result) {
                        is NotificationResult.Success -> _notifications.value = result.data
                        is NotificationResult.Failure -> showError("Error al obtener notificaciones: ${result.exception.message}")
                    }
                }
            } catch (e: Exception) {
                showError("Error inesperado: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Marcar una notificación como leída.
     */
    fun markAsRead(notificationId: String) {
        executeWithLoading {
            try {
                val success = notificationRepository.markAsRead(notificationId)
                if (success) {
                    _notifications.value = _notifications.value.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    }
                } else {
                    showError("No se pudo marcar la notificación como leída.")
                }
            } catch (e: Exception) {
                showError("Error al marcar la notificación como leída: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Eliminar una notificación.
     */
    fun deleteNotification(notificationId: String) {
        executeWithLoading {
            try {
                val success = notificationRepository.deleteNotification(notificationId)
                if (success) {
                    _notifications.value = _notifications.value.filter { it.id != notificationId }
                } else {
                    showError("No se pudo eliminar la notificación.")
                }
            } catch (e: Exception) {
                showError("Error al eliminar la notificación: ${e.localizedMessage}")
            }
        }
    }

    // ---- Utilidades para Manejo de Estados ----
    private fun showError(message: String) {
        _errorMessage.value = message
    }

    private fun executeWithLoading(operation: suspend () -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Ocurrió un error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
