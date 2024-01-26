package com.althaus.dev.project05_recetario.model

data class Recipe(val id: String, val name: String, val ingredients: Collection<Ingredient>)
