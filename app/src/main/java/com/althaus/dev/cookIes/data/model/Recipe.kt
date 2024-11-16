package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Recipe(
    @DocumentId val id: String = "",
    val name: String,
    val description: String = "",
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: String = "",
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val totalCalories: Int = 0,
    val servings: Int = 1,
    val cuisineType: String = "Desconocida",
    val difficultyLevel: Int = 3,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val tags: List<String> = emptyList(),
    val authorId: String? = null
) {
    init {
        require(name.isNotBlank()) { "El nombre de la receta no puede estar vacío." }
        require(difficultyLevel in 1..5) { "difficultyLevel debe estar entre 1 y 5." }
        require(prepTimeMinutes >= 0) { "prepTimeMinutes no puede ser negativo." }
        require(cookTimeMinutes >= 0) { "cookTimeMinutes no puede ser negativo." }
        require(totalCalories >= 0) { "totalCalories no puede ser negativo." }
        require(servings > 0) { "servings debe ser al menos 1." }
    }

    val totalTimeMinutes: Int
        get() = prepTimeMinutes + cookTimeMinutes

    val caloriesPerServing: Int
        get() = if (servings > 0) totalCalories / servings else 0

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "description" to description,
            "ingredients" to ingredients.map { it.toMap() },
            "instructions" to instructions,
            "prepTimeMinutes" to prepTimeMinutes,
            "cookTimeMinutes" to cookTimeMinutes,
            "totalCalories" to totalCalories,
            "servings" to servings,
            "cuisineType" to cuisineType,
            "difficultyLevel" to difficultyLevel,
            "imageUrl" to imageUrl,
            "videoUrl" to videoUrl,
            "tags" to tags,
            "authorId" to authorId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): Recipe {
            return Recipe(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                description = map["description"] as? String ?: "",
                ingredients = (map["ingredients"] as? List<Map<String, Any?>>)?.map { Ingredient.fromMap(it) } ?: emptyList(),
                instructions = map["instructions"] as? String ?: "",
                prepTimeMinutes = (map["prepTimeMinutes"] as? Number)?.toInt() ?: 0,
                cookTimeMinutes = (map["cookTimeMinutes"] as? Number)?.toInt() ?: 0,
                totalCalories = (map["totalCalories"] as? Number)?.toInt() ?: 0,
                servings = (map["servings"] as? Number)?.toInt() ?: 1,
                cuisineType = map["cuisineType"] as? String ?: "Desconocida",
                difficultyLevel = (map["difficultyLevel"] as? Number)?.toInt() ?: 3,
                imageUrl = map["imageUrl"] as? String,
                videoUrl = map["videoUrl"] as? String,
                tags = map["tags"] as? List<String> ?: emptyList(),
                authorId = map["authorId"] as? String
            )
        }
    }
}
