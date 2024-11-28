package com.althaus.dev.cookIes.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.ui.components.RecipeCard
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesView(
    recipeViewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by recipeViewModel.uiState.collectAsState()
    val favorites = uiState.favorites
    val isLoading = uiState.isLoading

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = "Mis Recetas Favoritas",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        SharedLoadingIndicator()
                    }
                    favorites.isEmpty() -> {
                        Text(
                            text = "No tienes recetas favoritas.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    else -> {
                        LazyColumn(
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
    )
}
