package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.data.model.Recipe

@Composable
fun RecipeListView(
    recipes: List<Recipe>,              // Lista de recetas a mostrar
    onRecipeClick: (String) -> Unit,   // Acción al hacer clic en una receta
    emptyMessage: String = "No hay recetas disponibles." // Mensaje en caso de lista vacía
) {
    if (recipes.isEmpty()) {
        // Mostrar mensaje si no hay recetas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        // Mostrar la lista de recetas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(recipes) { recipe ->
                // Aquí cada receta se renderiza en su propia Card
                RecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe.id ?: "") }
                )
            }
        }
    }
}
