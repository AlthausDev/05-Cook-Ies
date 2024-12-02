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

/**
 * Composable que representa la vista de recetas favoritas del usuario.
 *
 * Muestra una lista de recetas marcadas como favoritas por el usuario. Si no hay recetas
 * favoritas, se muestra un mensaje indicándolo. También maneja el estado de carga
 * mientras se obtienen las recetas desde el ViewModel.
 *
 * @param recipeViewModel [RecipeViewModel] que gestiona el estado y las acciones relacionadas con las recetas.
 * @param onRecipeClick Acción que se ejecuta al seleccionar una receta, recibe el ID de la receta como parámetro.
 * @param onBack Acción que se ejecuta al presionar el botón de retroceso en la barra superior.
 */
@Composable
fun FavoritesView(
    recipeViewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit
)
 {
    val uiState by recipeViewModel.uiState.collectAsState()
    val favorites = uiState.favorites
    val isLoading = uiState.isLoading

    Scaffold(
        topBar = {
            /**
             * Barra superior personalizada ([SharedTopAppBar]) con un título y un botón para volver atrás.
             *
             * - Título: "Mis Recetas Favoritas".
             * - Icono de navegación: Flecha hacia atrás ([Icons.Default.ArrowBack]), que ejecuta la acción `onBack`.
             */
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
        /**
         * Contenedor principal que muestra diferentes estados dependiendo del estado de `uiState`:
         *
         * - Cargando: Muestra un indicador de carga ([SharedLoadingIndicator]).
         * - Sin favoritos: Muestra un mensaje indicando que no hay recetas favoritas.
         * - Lista de favoritos: Muestra una lista de recetas favoritas utilizando [LazyColumn].
         */
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
                        /**
                         * Indicador de carga ([SharedLoadingIndicator]) que se muestra mientras `uiState.isLoading` es verdadero.
                        */
                        SharedLoadingIndicator()
                    }
                    favorites.isEmpty() -> {
                        /**
                         * Texto centrado indicando que no hay recetas favoritas disponibles.
                         *
                         * Este estado se muestra si `uiState.favorites` está vacío.
                         */
                        Text(
                            text = "No tienes recetas favoritas.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    else -> {
                        /**
                         * Lista de recetas favoritas mostrada con un [LazyColumn].
                         *
                         * - Cada receta se representa mediante un [RecipeCard].
                         * - Al hacer clic en una receta, se llama a `onRecipeClick` con el ID de la receta.
                         *
                         * @param favorites Lista de recetas favoritas obtenida del ViewModel.
                         */
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
