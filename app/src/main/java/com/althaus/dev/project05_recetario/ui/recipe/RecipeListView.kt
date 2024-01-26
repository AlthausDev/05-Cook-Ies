package com.althaus.dev.project05_recetario.ui.recipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecipeListView(recipeViewModel: RecipeViewModel = viewModel()) {
    val recipes = recipeViewModel.recipes.collectAsState()
}
