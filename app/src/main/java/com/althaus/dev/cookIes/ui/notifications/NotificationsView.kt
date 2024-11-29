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

@Composable
fun NotificationsView(
    notificationsViewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val notificationsState = notificationsViewModel.notifications.collectAsState()
    val isLoading = notificationsViewModel.isLoading.collectAsState()
    val errorMessage = notificationsViewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
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
                isLoading.value -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
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
                notificationsState.value.isNullOrEmpty() -> {
                    Text(
                        text = "No hay notificaciones disponibles",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    // Generar una clave única basada en el estado de las notificaciones
                    val refreshKey = notificationsState.value.hashCode()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = notificationsState.value,
                            key = { notification -> "${notification.id}-$refreshKey" } // Forzar recomposición con hash
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
