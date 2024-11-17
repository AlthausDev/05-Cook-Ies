package com.althaus.dev.cookIes.data.model

import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@IgnoreExtraProperties
data class Ingredient(
    @DocumentId val id: String = "", // ID del documento
    val name: String = "", // Nombre del ingrediente
    val quantity: Double = 1.0, // Cantidad
    val unit: String = "unidad", // Unidad de medida (por ejemplo, gramos, litros)
    val description: String = "", // Descripción opcional
    val isAllergen: Boolean = false, // Indica si es un alérgeno
    val substitutes: List<String> = emptyList(), // Lista de sustitutos
    val category: String = "", // Categoría (por ejemplo, frutas, especias)
    val calories: Double = 0.0 // Calorías (valor predeterminado de 0.0)
) {
    init {
        // Validaciones para evitar datos inválidos
        require(name.isNotBlank()) { "El nombre del ingrediente no puede estar vacío." }
        require(quantity >= 0) { "La cantidad no puede ser negativa." }
    }

    // Conversión del objeto a un Map para Firestore
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "quantity" to quantity,
            "unit" to unit,
            "description" to description,
            "isAllergen" to isAllergen,
            "substitutes" to substitutes,
            "category" to category,
            "calories" to calories
        )
    }

    // Guardar ingrediente en Firestore
    suspend fun saveToFirestore(repository: FirestoreRepository) {
        try {
            val ingredientId = if (id.isBlank()) repository.generateNewId("ingredients") else id
            repository.saveIngredient(ingredientId, toMap())
        } catch (e: Exception) {
            throw Exception("Error al guardar el ingrediente en Firestore: ${e.localizedMessage}")
        }
    }

    // Actualizar campos específicos en Firestore
    suspend fun updateInFirestore(repository: FirestoreRepository, updates: Map<String, Any>) {
        try {
            if (id.isBlank()) throw IllegalArgumentException("No se puede actualizar un ingrediente sin ID.")
            repository.updateIngredient(id, updates)
        } catch (e: Exception) {
            throw Exception("Error al actualizar el ingrediente en Firestore: ${e.localizedMessage}")
        }
    }

    companion object {
        // Conversión de un Map a un objeto Ingredient
        fun fromMap(map: Map<String, Any?>): Ingredient {
            return Ingredient(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                quantity = (map["quantity"] as? Number)?.toDouble() ?: 1.0,
                unit = map["unit"] as? String ?: "unidad",
                description = map["description"] as? String ?: "",
                isAllergen = map["isAllergen"] as? Boolean ?: false,
                substitutes = (map["substitutes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                category = map["category"] as? String ?: "",
                calories = (map["calories"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}
