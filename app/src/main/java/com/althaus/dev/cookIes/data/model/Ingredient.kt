package com.althaus.dev.cookIes.data.model

data class Ingredient(
    val id: String? = null,
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val description: String? = null,
    val isAllergen: Boolean = false,
    val substitutes: List<String>? = null
)
