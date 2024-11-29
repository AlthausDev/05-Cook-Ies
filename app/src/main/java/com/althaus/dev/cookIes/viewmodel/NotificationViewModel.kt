import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadNotifications()
    }

    /**
     * Carga las notificaciones del usuario actual desde Firestore.
     */
    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw Exception("Usuario no autenticado")
                _notifications.value = firestoreRepository.getNotifications(userId)
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar notificaciones: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Marca una notificación como leída.
     */
    fun markAsRead(notification: Notification) {
        viewModelScope.launch {
            try {
                // Actualizar Firestore
                firestoreRepository.updateNotification(
                    notification.id,
                    mapOf("read" to true)
                )

                // Actualizar el estado localmente, creando una nueva lista para forzar recomposición
                _notifications.value = _notifications.value.map {
                    if (it.id == notification.id) it.copy(read = true) else it
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al marcar como leída: ${e.localizedMessage}"
            }
        }
    }


    /**
     * Limpia el mensaje de error.
     */
    fun resetError() {
        _errorMessage.value = null
    }
}
