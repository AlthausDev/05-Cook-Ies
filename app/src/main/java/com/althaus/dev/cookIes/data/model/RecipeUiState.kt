package com.althaus.dev.cookIes.data.model

data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val favorites: List<Recipe> = emptyList(),
    val selectedRecipe: Recipe? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userRating: Float? = null // Nueva propiedad para la calificación del usuario
)
