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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsView(
    notificationsViewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val notificationsState = notificationsViewModel.notifications.collectAsState()
    val isLoading = notificationsViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
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
                    CircularProgressIndicator()
                }
                notificationsState.value.isNullOrEmpty() -> {
                    Text(
                        text = "No hay notificaciones disponibles",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notificationsState.value) { notification ->
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

@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: (Notification) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título de la notificación
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            // Mensaje de la notificación
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )
            // Fecha legible de la notificación
            Text(
                text = notification.getReadableTimestamp(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
            // Botón para marcar como leída
            if (!notification.isRead) {
                Button(
                    onClick = { onMarkAsRead(notification) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Marcar como leída") // Este texto podría internacionalizarse
                }
            }
        }
    }
}

