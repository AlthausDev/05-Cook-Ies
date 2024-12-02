import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar las notificaciones del usuario.
 *
 * Este ViewModel se encarga de cargar, actualizar y marcar como leídas las notificaciones
 * almacenadas en Firestore para el usuario autenticado.
 *
 * @property firestoreRepository Repositorio para interactuar con Firestore.
 */
class NotificationsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    /**
     * Flujo de estado que contiene la lista de notificaciones.
     */
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    /**
     * Flujo de estado que indica si se está realizando una operación de carga.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Flujo de estado que contiene el mensaje de error actual, si existe.
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Inicializa el ViewModel cargando las notificaciones del usuario actual.
     */
    init {
        loadNotifications()
    }

    /**
     * Carga las notificaciones del usuario actual desde Firestore.
     *
     * Este método obtiene el identificador único del usuario autenticado y recupera
     * sus notificaciones desde el repositorio. Si ocurre un error, se almacena un
     * mensaje de error en el flujo correspondiente.
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
     * Marca una notificación específica como leída.
     *
     * Este método actualiza el estado de la notificación en Firestore y
     * en la lista local almacenada en el flujo de estado. Si ocurre un error,
     * se almacena un mensaje de error en el flujo correspondiente.
     *
     * @param notification La notificación que se desea marcar como leída.
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
}
