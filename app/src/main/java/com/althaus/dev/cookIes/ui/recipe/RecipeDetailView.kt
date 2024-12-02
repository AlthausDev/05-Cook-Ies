package com.althaus.dev.cookIes.ui.recipe

import android.content.Context
import android.content.Intent
import android.widget.RatingBar
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar

import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

@Composable
fun RecipeDetailView(
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    onFavorite: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.selectedRecipe
    val context = LocalContext.current

    // Estado local para las puntuaciones
    var userRating by remember { mutableStateOf(0.0) }
    var hasUserRatingChanged by remember { mutableStateOf(false) }

    var isFavorite by remember { mutableStateOf(false) }


    // Cargar la puntuación del usuario cuando cambia la receta
    LaunchedEffect(recipe?.id) {
        recipe?.id?.let { recipeId ->
            userRating = viewModel.getUserRatingForRecipe(recipeId) ?: 0.0
            viewModel.refreshFavorites() // Refrescar favoritos
            isFavorite = viewModel.uiState.value.favorites.any { it.id == recipeId }
        }
    }

    // Enviar la calificación al ViewModel solo cuando cambie
    LaunchedEffect(userRating) {
        if (hasUserRatingChanged && recipe != null) {
            viewModel.rateRecipe(recipe.id, userRating)
            hasUserRatingChanged = false // Restablecer bandera tras actualizar
        }
    }


    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = recipe?.name ?: "Cargando receta...",
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasUserRatingChanged && recipe != null) {
                                viewModel.rateRecipe(recipe.id, userRating)
                            }
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            recipe?.let {
                                if (isFavorite) {
                                    viewModel.removeFromFavorites(it.id)
                                } else {
                                    viewModel.addToFavorites(it.id)
                                }
                                isFavorite = !isFavorite // Cambiar el estado localmente
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Agregar o quitar de favoritos",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { recipe?.let { shareRecipe(context, it) } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir receta"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            when {
                uiState.isLoading -> {
                    SharedLoadingIndicator()
                }
                recipe != null -> {
                    RecipeDetailContent(
                        recipe = recipe,
                        userRating = userRating,
                        onUserRatingChange = { newRating ->
                            userRating = newRating
                            hasUserRatingChanged = true
                        },
                        modifier = Modifier.padding(innerPadding) // Respeta el padding del Scaffold
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Receta no encontrada",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

    )
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    userRating: Double,
    onUserRatingChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Espaciado uniforme
    val sectionSpacing = 24.dp
    val lineHeight = 22.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Imagen de la receta
        recipe.imageUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Imagen de ${recipe.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(modifier = Modifier.height(sectionSpacing))

        // Nombre de la receta
        Text(
            text = recipe.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Text(
            text = "Descripción:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = recipe.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(sectionSpacing))
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(sectionSpacing))

        // Ingredientes
        if (recipe.ingredients.isNotEmpty()) {
            Text(
                text = "Ingredientes:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            recipe.ingredients.forEach { ingredient ->
                Text(
                    text = "- ${ingredient.name}: ${ingredient.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(sectionSpacing))
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(sectionSpacing))

        // Instrucciones
        Text(
            text = "Instrucciones:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = recipe.instructions,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(sectionSpacing))
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))


        // Tiempo total de preparación
        Text(
            text = "Tiempo total: ${recipe.totalTimeMinutes} minutos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(sectionSpacing))

        Spacer(modifier = Modifier.height(sectionSpacing))

        // Puntuación promedio
        Text(
            text = "Puntuación promedio:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color.Yellow, Color.Green)),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 8.dp) // Ajusta el padding interno
        ) {
            // Texto de la puntuación promedio centrado
            Text(
                text = String.format("%.2f / 10", recipe.averageRating * 2), // Escalamos la puntuación
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.Center) // Centramos el texto
            )

            // Texto del total de votos alineado a la derecha
            Text(
                text = "(${recipe.ratingCount} votos)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp) // Separación del borde derecho
            )
        }

        Spacer(modifier = Modifier.height(sectionSpacing))

        // Puntuación del usuario
        Text(
            text = "Tu puntuación:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        RatingBar(
            rating = userRating,
            onRatingChanged = onUserRatingChange,
            isEnabled = true
        )
    }
}

@Composable
fun TagItem(tag: String) {
    Text(
        text = "#$tag",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

// Función para compartir una receta
fun shareRecipe(context: Context, recipe: Recipe) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            """
                ¡Prueba esta receta increíble!
                Nombre: ${recipe.name}
                Descripción: ${recipe.description}
                Tiempo de preparación: ${recipe.prepTimeMinutes} minutos
                Tiempo de cocción: ${recipe.cookTimeMinutes} minutos
                Calorías: ${recipe.totalCalories} kcal
                Porciones: ${recipe.servings}
                
                ¡Descárgala en nuestra app para más recetas como esta!
            """.trimIndent()
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Compartir receta vía"))
}

@Composable
fun RatingBar(
    rating: Double,
    onRatingChanged: (Double) -> Unit,
    isEnabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..5) {
                val isActive = i <= rating
                val animatedGlow = remember { Animatable(0.1f) } // Controla la intensidad del brillo

                // Animar el brillo para las estrellas activas
                LaunchedEffect(isActive) {
                    if (isActive) {
                        animatedGlow.animateTo(
                            targetValue = 0.5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    } else {
                        animatedGlow.snapTo(0.5f)
                    }
                }

                IconButton(
                    onClick = {
                        if (isEnabled) onRatingChanged(i.toDouble())
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer { // Agrega efectos visuales avanzados
                                if (isActive) {
                                    shadowElevation = 12.dp.toPx() // Intensidad de la sombra
                                    shape = RoundedCornerShape(50) // Forma redondeada
                                    alpha = 0.7f + animatedGlow.value * 0.1f // Brillo animado
                                }
                            }
                            .background(
                                brush = if (isActive)
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Yellow.copy(alpha = 0.8f),
                                            Color.Transparent
                                        ),
                                        radius = animatedGlow.value * 100f
                                    )
                                else Brush.linearGradient(
                                    colors = listOf(Color.Gray, Color.Gray)
                                ),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Estrella $i",
                            tint = if (isActive) Color.Yellow else Color.Gray,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
