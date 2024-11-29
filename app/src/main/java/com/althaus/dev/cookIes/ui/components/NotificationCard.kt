package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.data.model.Notification

@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: (Notification) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Mantiene el padding externo siempre
            .border(
                width = if (notification.read) 1.dp else 2.dp,
                color = if (notification.read) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = if (notification.read) {
            CardDefaults.cardElevation(0.dp)
        } else {
            CardDefaults.cardElevation(6.dp)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f) // Color para notificaciones leídas
            } else {
                MaterialTheme.colorScheme.surface // Color para no leídas
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Siempre tiene padding interno
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título de la notificación
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (notification.read) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Texto de color primary con alpha aumentado
                } else {
                    MaterialTheme.colorScheme.primary
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Mensaje de la notificación
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notification.read) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Texto del mensaje con alpha aumentado
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 4,
                overflow = TextOverflow.Clip
            )

            // Fecha/hora de la notificación
            notification.readableTimestamp?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notification.read) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Fecha/hora con alpha aumentado
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Botón de acción, ubicado al final
            if (!notification.read) { // Ocultar el botón si la notificación ya está leída
                PrimaryButton(
                    onClick = { onMarkAsRead(notification) },
                    text = "Marcar como leída",
                    modifier = Modifier
                        .align(Alignment.End) // Alineado a la derecha
                        .padding(top = 8.dp)
                )
            }
        }
    }

}
