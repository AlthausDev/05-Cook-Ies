package com.althaus.dev.cookIes.data.model

data class Recipe(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: String? = null,
    val prepTime: Int? = null,
    val cookTime: Int? = null,
    val totalCalories: Int? = null,
    val servings: Int? = null,
    val cuisineType: String? = null,
    val difficultyLevel: Int = 3,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val tags: List<String>? = null,
    val author: String? = null
) {
    init {
        require(difficultyLevel in 1..5) { "difficultyLevel debe estar entre 1 y 5." }
    }
}
