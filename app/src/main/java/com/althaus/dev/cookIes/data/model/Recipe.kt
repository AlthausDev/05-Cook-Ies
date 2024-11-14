package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Recipe(
    @DocumentId val id: String = "",              // ID único de la receta en Firestore
    val name: String,                             // Nombre de la receta
    val description: String = "",                 // Breve descripción de la receta
    val ingredients: List<Ingredient> = emptyList(), // Lista de ingredientes (inmutable)
    val instructions: String = "",                // Instrucciones de preparación
    val prepTimeMinutes: Int = 0,                 // Tiempo de preparación en minutos
    val cookTimeMinutes: Int = 0,                 // Tiempo de cocción en minutos
    val totalCalories: Int = 0,                   // Total de calorías
    val servings: Int = 1,                        // Porciones (predeterminado a 1)
    val cuisineType: String = "Desconocida",      // Tipo de cocina (ej. Italiana, Mexicana)
    val difficultyLevel: Int = 3,                 // Nivel de dificultad (1-5)
    val imageUrl: String? = null,                 // URL de la imagen de la receta
    val videoUrl: String? = null,                 // URL de un video de preparación (opcional)
    val tags: List<String> = emptyList(),         // Lista de etiquetas o categorías (inmutable)
    val authorId: String? = null                  // ID del autor (usuario que creó la receta)
) {
    init {
        require(difficultyLevel in 1..5) { "difficultyLevel debe estar entre 1 y 5." }
        require(prepTimeMinutes >= 0) { "prepTimeMinutes no puede ser negativo." }
        require(cookTimeMinutes >= 0) { "cookTimeMinutes no puede ser negativo." }
        require(totalCalories >= 0) { "totalCalories no puede ser negativo." }
        require(servings > 0) { "servings debe ser al menos 1." }
    }
}
