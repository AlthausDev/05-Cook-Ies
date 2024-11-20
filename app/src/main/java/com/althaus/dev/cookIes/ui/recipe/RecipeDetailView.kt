package com.althaus.dev.cookIes.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.althaus.dev.cookIes.theme.ErrorLight
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.PrimaryLight
import com.althaus.dev.cookIes.theme.SecondaryLight
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailView(
    viewModel: RecipeViewModel,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recipe = uiState.selectedRecipe

    // Estado local para la puntuación del usuario
    val (localRating, setLocalRating) = remember(recipe?.averageRating) {
        mutableStateOf(recipe?.averageRating?.takeIf { it > 0 } ?: 0f)
    }

    // Estado para manejar si la puntuación cambió
    var hasRatingChanged by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = recipe?.name ?: "Cargando receta...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Si hubo cambios, actualizamos la puntuación antes de salir
                            if (hasRatingChanged && recipe != null) {
                                viewModel.rateRecipe(recipe.id, localRating)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark
                )
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
                            tint = if (uiState.favorites.any { it.id == recipe?.id }) ErrorLight else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = onShare) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(colors = listOf(PrimaryLight, SecondaryLight)))
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

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

                        // Lista de ingredientes
                        Text("Ingredientes:", style = MaterialTheme.typography.titleMedium)
                        recipe.ingredients.forEach { ingredient ->
                            Text("- ${ingredient.name}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Instrucciones de la receta
                        Text("Instrucciones:", style = MaterialTheme.typography.titleMedium)
                        Text(recipe.instructions)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Etiquetas de la receta
                        if (recipe.tags.isNotEmpty()) {
                            Text("Etiquetas: ${recipe.tags.joinToString(", ")}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Barra de calificación
                        RatingBar(
                            rating = localRating,
                            onRatingChanged = { newRating ->
                                setLocalRating(newRating)
                                hasRatingChanged = true // Marcamos que hubo cambios
                            }
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

@Composable
fun RatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            IconButton(onClick = { onRatingChanged(i.toFloat()) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rating $i",
                    tint = if (i <= rating) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

