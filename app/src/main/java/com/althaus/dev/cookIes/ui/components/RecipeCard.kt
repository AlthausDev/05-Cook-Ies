package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.Recipe

/**
 * Composable que representa una tarjeta para mostrar información de una receta.
 *
 * La tarjeta incluye una imagen, el nombre de la receta, una descripción breve y detalles
 * adicionales como el tiempo de preparación y el tipo de cocina. La tarjeta es interactiva
 * y permite al usuario hacer clic para realizar una acción personalizada.
 *
 * @param recipe Objeto [Recipe] que contiene los detalles de la receta que se mostrará.
 * @param onClick Lambda que se ejecuta cuando el usuario hace clic en la tarjeta.
 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // La tarjeta ocupa todo el ancho disponible
            .clickable { onClick() } // Acción al hacer clic en la tarjeta
            .padding(vertical = 8.dp) // Espaciado vertical alrededor de la tarjeta
            .border(
                width = 2.dp, // Borde alrededor de la tarjeta
                color = MaterialTheme.colorScheme.secondary, // Color del borde
                shape = RoundedCornerShape(12.dp) // Esquinas redondeadas
            ),
        shape = RoundedCornerShape(12.dp), // Forma redondeada de la tarjeta
        elevation = CardDefaults.cardElevation(6.dp), // Elevación para efecto de sombra
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 1.0f), // Color de fondo
            contentColor = MaterialTheme.colorScheme.onSurface // Color del contenido
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // El contenido ocupa todo el ancho de la tarjeta
                .height(120.dp) // Altura fija de la tarjeta
                .padding(12.dp) // Espaciado interno
        ) {
            // Imagen de la receta
            Image(
                painter = if (recipe.imageUrl.isNullOrEmpty()) {
                    painterResource(id = R.drawable.default_profile) // Imagen predeterminada si no hay URL
                } else {
                    rememberAsyncImagePainter(recipe.imageUrl) // Imagen cargada desde la URL
                },
                contentDescription = "Imagen de ${recipe.name}", // Descripción de accesibilidad
                contentScale = ContentScale.Crop, // Ajuste de la imagen
                modifier = Modifier
                    .size(100.dp) // Tamaño de la imagen
                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas para la imagen
            )

            Spacer(modifier = Modifier.width(12.dp)) // Espaciado entre la imagen y el texto

            Column(
                modifier = Modifier
                    .fillMaxHeight() // La columna ocupa toda la altura disponible
                    .weight(1f), // La columna ocupa el espacio restante
                verticalArrangement = Arrangement.SpaceBetween // Espaciado uniforme entre elementos
            ) {
                // Nombre de la receta
                Text(
                    text = recipe.name, // Título de la receta
                    style = MaterialTheme.typography.titleMedium, // Estilo del texto
                    color = MaterialTheme.colorScheme.primary, // Color del texto
                    maxLines = 1, // Limitar a una línea
                    overflow = TextOverflow.Ellipsis // Mostrar puntos suspensivos si excede el espacio
                )

                // Descripción de la receta
                Text(
                    text = recipe.description.takeIf { it.isNotBlank() } ?: "Sin descripción", // Texto predeterminado si está vacío
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, // Limitar a dos líneas
                    overflow = TextOverflow.Ellipsis
                )

                // Detalles adicionales: tiempo de preparación y tipo de cocina
                Row(
                    verticalAlignment = Alignment.CenterVertically, // Alinear al centro vertical
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Espaciado entre íconos y texto
                ) {
                    Icon(
                        imageVector = Icons.Default.Info, // Ícono de información
                        contentDescription = "Tiempo de preparación", // Descripción para accesibilidad
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, // Color del ícono
                        modifier = Modifier.size(16.dp) // Tamaño del ícono
                    )
                    Text(
                        text = "${recipe.prepTimeMinutes ?: 0} min", // Tiempo de preparación en minutos
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = recipe.cuisineType.takeIf { it.isNotBlank() } ?: "Desconocido", // Tipo de cocina o texto predeterminado
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
