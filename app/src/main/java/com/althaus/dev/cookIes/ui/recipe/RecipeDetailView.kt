package com.althaus.dev.cookIes.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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
    val isFavorite = uiState.favorites.any { it.id == recipe?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.selectedRecipe?.name ?: "Cargando receta...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                        Spacer(modifier = Modifier.weight(1f, true))
                        IconButton(
                            onClick = {
                                val isFavorite = uiState.favorites.any { it.id == recipe!!.id }
                                if (isFavorite) {
                                    viewModel.removeFromFavorites(recipe!!.id)
                                } else {
                                    viewModel.addToFavorites(recipe!!.id)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Agregar o quitar de favoritos",
                                tint = if (uiState.favorites.any { it.id == recipe!!.id }) Color.Red else Color.Gray
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
                uiState.selectedRecipe != null -> {
                    val recipe = uiState.selectedRecipe!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(colors = listOf(PrimaryLight, SecondaryLight)))
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Imagen de la receta si está disponible
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
                           // color = PrimaryDark.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Información general de la receta
                        Text(
                            text = "Tiempo de preparación: ${recipe.prepTimeMinutes} minutos",
                            style = MaterialTheme.typography.bodyMedium
                            //color = PrimaryDark
                        )
                        Text(
                            text = "Tiempo de cocción: ${recipe.cookTimeMinutes} minutos",
                            style = MaterialTheme.typography.bodyMedium
                           // color = PrimaryDark
                        )
                        Text(
                            text = "Calorías: ${recipe.totalCalories} kcal",
                            style = MaterialTheme.typography.bodyMedium
                            //color = PrimaryDark
                        )
                        Text(
                            text = "Porciones: ${recipe.servings}",
                            style = MaterialTheme.typography.bodyMedium
                            //color = PrimaryDark
                        )
                        Text(
                            text = "Nivel de dificultad: ${recipe.difficultyLevel}/5",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryDark
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lista de ingredientes
                        Text(
                            text = "Ingredientes:",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryDark
                        )
                        recipe.ingredients.forEach { ingredient ->
                            Text(
                                text = "- ${ingredient.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryDark.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Instrucciones de la receta
                        Text(
                            text = "Instrucciones:",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryDark
                        )
                        Text(
                            text = recipe.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryDark.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Etiquetas de la receta
                        if (recipe.tags.isNotEmpty()) {
                            Text(
                                text = "Etiquetas: ${recipe.tags.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryDark.copy(alpha = 0.7f)
                            )
                        }
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
