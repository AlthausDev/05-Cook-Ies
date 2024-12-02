package com.althaus.dev.cookIes.ui.notifications

import NotificationsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.ui.components.NotificationCard
import com.althaus.dev.cookIes.ui.components.PrimaryButton
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar

/**
 * Vista de notificaciones que muestra una lista de las notificaciones del usuario.
 *
 * Muestra el estado de carga, mensajes de error y una lista de notificaciones.
 * También permite marcar las notificaciones como leídas y recargar la lista en caso de error.
 *
 * @param notificationsViewModel [NotificationsViewModel] que proporciona el estado y las acciones relacionadas con las notificaciones.
 * @param onBack Acción que se ejecuta al presionar el botón de retroceso en la barra superior.
 */
@Composable
fun NotificationsView(
    notificationsViewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    // Estado de las notificaciones, carga y mensajes de error
    val notificationsState = notificationsViewModel.notifications.collectAsState()
    val isLoading = notificationsViewModel.isLoading.collectAsState()
    val errorMessage = notificationsViewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            /**
             * Barra superior personalizada ([SharedTopAppBar]) con un título y un botón de retroceso.
             *
             * - Título: "Notificaciones".
             * - Icono de navegación: Flecha hacia atrás ([Icons.Default.ArrowBack]) que ejecuta la acción `onBack`.
             */
            SharedTopAppBar(
                title = "Notificaciones",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                /**
                 * Estado de carga:
                 * Muestra un indicador de progreso circular mientras se cargan las notificaciones.
                 */
                isLoading.value -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                /**
                 * Estado de error:
                 * Muestra un mensaje de error y un botón para reintentar cargar las notificaciones.
                 */
                errorMessage.value != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = errorMessage.value ?: "Error desconocido",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        PrimaryButton(
                            onClick = { notificationsViewModel.loadNotifications() },
                            text = "Reintentar"
                        )
                    }
                }
                /**
                 * Estado vacío:
                 * Muestra un mensaje indicando que no hay notificaciones disponibles.
                 */
                notificationsState.value.isNullOrEmpty() -> {
                    Text(
                        text = "No hay notificaciones disponibles",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                /**
                 * Estado con datos:
                 * Muestra una lista de notificaciones utilizando [LazyColumn].
                 * Cada notificación se representa mediante [NotificationCard].
                 */
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = notificationsState.value,
                            key = { notification -> notification.id }
                        ) { notification ->
                            NotificationCard(
                                notification = notification,
                                onMarkAsRead = {
                                    notificationsViewModel.markAsRead(notification)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
