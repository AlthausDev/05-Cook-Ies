package com.althaus.dev.cookIes.data.model

data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val favorites: List<Recipe> = emptyList(),
    val selectedRecipe: Recipe? = null,
    val userRatings: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = false,
    val error: UiError? = null // Mejor manejo de errores
)

sealed class UiError {
    object NetworkError : UiError()
    object AuthError : UiError()
    data class UnknownError(val message: String) : UiError()
}
