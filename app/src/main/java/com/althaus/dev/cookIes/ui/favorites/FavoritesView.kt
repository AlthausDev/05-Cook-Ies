package com.althaus.dev.cookIes.ui.favorites

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
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel
import com.althaus.dev.cookIes.ui.components.RecipeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesView(
    recipeViewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState = recipeViewModel.uiState.collectAsState().value
    val favorites = uiState.favorites
    val isLoading = uiState.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritas") },
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
                isLoading -> {
                    CircularProgressIndicator()
                }
                favorites.isEmpty() -> {
                    Text(
                        text = "No tienes recetas favoritas.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favorites) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { recipe.id?.let(onRecipeClick) }
                            )
                        }
                    }
                }
            }
        }
    }
}
