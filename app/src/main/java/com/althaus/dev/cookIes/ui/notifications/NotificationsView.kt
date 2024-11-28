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

@OptIn(ExperimentalMaterial3Api::class)
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
                        Button(onClick = { notificationsViewModel.loadNotifications() }) {
                            Text("Reintentar")
                        }
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
