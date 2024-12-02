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

/**
 * Composable que representa una tarjeta de notificación.
 *
 * Esta tarjeta muestra el título, mensaje y fecha/hora de una notificación.
 * Permite al usuario marcar la notificación como leída si no lo está.
 * El estilo visual de la tarjeta varía dependiendo del estado de lectura de la notificación.
 *
 * @param notification Objeto [Notification] que contiene los datos de la notificación.
 * @param onMarkAsRead Callback que se ejecuta cuando el usuario marca la notificación como leída.
 */
@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: (Notification) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp) // Espaciado vertical entre las tarjetas
            .border(
                width = if (notification.read) 1.dp else 2.dp, // Borde más grueso para no leídas
                color = if (notification.read)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = if (notification.read) {
            CardDefaults.cardElevation(0.dp) // Sin elevación para leídas
        } else {
            CardDefaults.cardElevation(6.dp) // Elevación para destacar las no leídas
        },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read) {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f) // Color de fondo para leídas
            } else {
                MaterialTheme.colorScheme.surface // Color de fondo para no leídas
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Margen interno de la tarjeta
            verticalArrangement = Arrangement.spacedBy(8.dp) // Espaciado entre elementos
        ) {
            // ---- Título ----
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (notification.read) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Color apagado para leídas
                } else {
                    MaterialTheme.colorScheme.primary // Color destacado para no leídas
                },
                maxLines = 2, // Limitar a 2 líneas
                overflow = TextOverflow.Ellipsis // Mostrar puntos suspensivos si excede el espacio
            )

            // ---- Mensaje ----
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (notification.read) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Color apagado para leídas
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant // Color para no leídas
                },
                maxLines = 4, // Limitar a 4 líneas
                overflow = TextOverflow.Clip // No mostrar puntos suspensivos
            )

            // ---- Fecha/hora ----
            notification.readableTimestamp?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (notification.read) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Color apagado para leídas
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant // Color para no leídas
                    }
                )
            }

            // ---- Botón de acción ----
            if (!notification.read) { // Mostrar solo si no está leída
                PrimaryButton(
                    onClick = { onMarkAsRead(notification) }, // Marcar como leída
                    text = "Marcar como leída",
                    modifier = Modifier
                        .align(Alignment.End) // Alineación a la derecha
                        .padding(top = 8.dp) // Espaciado superior
                )
            }
        }
    }
}
