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

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                _notifications.value = firestoreRepository.getNotifications(userId)
            } catch (e: Exception) {
                // Manejo de errores, si es necesario
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun markAsRead(notification: Notification) {
        viewModelScope.launch {
            try {
                val updatedNotification = notification.markAsRead()
                firestoreRepository.updateNotification(updatedNotification.id, mapOf("isRead" to true))
                _notifications.value = _notifications.value.map {
                    if (it.id == updatedNotification.id) updatedNotification else it
                }
            } catch (e: Exception) {
                // Manejar el error si es necesario
            }
        }
    }
}

