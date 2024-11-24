package com.althaus.dev.cookIes.ui.recipe

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.althaus.dev.cookIes.data.model.Recipe

import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    // Cargar la puntuación del usuario cuando cambia la receta
    LaunchedEffect(recipe?.id) {
        recipe?.id?.let { recipeId ->
            userRating = viewModel.getUserRatingForRecipe(recipeId) ?: 0.0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recipe?.name ?: "Cargando receta...",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
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
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            BottomAppBar(
                content = {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            if (recipe != null) {
                                val isFavorite = uiState.favorites.any { it.id == recipe.id }
                                if (isFavorite) {
                                    viewModel.removeFromFavorites(recipe.id)
                                } else {
                                    viewModel.addToFavorites(recipe.id)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Agregar o quitar de favoritos",
                            tint = if (uiState.favorites.any { it.id == recipe?.id }) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = {
                            recipe?.let { shareRecipe(context, it) }
                        }
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                recipe != null -> {
                    // Contenido desplazable
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()) // Habilitar desplazamiento
                            .padding(innerPadding)
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Descripción de la receta
                        Text(
                            text = "Descripción:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = recipe.description,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Información general de la receta
                        Text("Tiempo de preparación: ${recipe.prepTimeMinutes} minutos")
                        Text("Tiempo de cocción: ${recipe.cookTimeMinutes} minutos")
                        Text("Calorías: ${recipe.totalCalories} kcal")
                        Text("Porciones: ${recipe.servings}")
                        Text("Nivel de dificultad: ${recipe.difficultyLevel}/5")

                        Spacer(modifier = Modifier.height(16.dp))

                        // Puntuación promedio
                        Text(
                            text = "Puntuación promedio: " + String.format("%.2f", recipe.averageRating),
                            style = MaterialTheme.typography.titleMedium
                        )

                        RatingBar(
                            rating = recipe.averageRating, // Usamos la puntuación promedio directamente
                            onRatingChanged = {}, // No interactiva
                            isEnabled = false
                        )
                        Text(
                            text = "(${recipe.ratingCount} votos)", // Mostrar total de votos
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )


                        Spacer(modifier = Modifier.height(8.dp))

                        // Puntuación del usuario
                        Text(
                            text = "Tu puntuación:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        RatingBar(
                            rating = userRating,
                            onRatingChanged = { newRating ->
                                userRating = newRating
                                hasUserRatingChanged = true
                                recipe?.id?.let { recipeId ->
                                    viewModel.rateRecipe(recipeId, newRating)
                                }
                            },
                            isEnabled = true
                        )

                    }
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
    isEnabled: Boolean = true // Determina si la barra es interactiva
) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            IconButton(
                onClick = {
                    if (isEnabled) onRatingChanged(i.toDouble())
                }
            ) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Estrella $i",
                    tint = if (i <= rating) Color.Yellow else Color.Gray, // Cambiar estrellas seleccionadas a amarillo
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
