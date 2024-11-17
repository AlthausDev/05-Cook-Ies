package com.althaus.dev.cookIes.data.model

import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@IgnoreExtraProperties
data class Ingredient(
    @DocumentId val id: String = "",
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "unidad",
    val description: String = "",
    val isAllergen: Boolean = false,
    val substitutes: List<String> = emptyList(),
    val category: String = "",
    val calories: Double? = null
) {
    init {
        require(name.isNotBlank()) { "El nombre del ingrediente no puede estar vacío." }
        require(quantity >= 0) { "La cantidad no puede ser negativa." }
    }

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
            "calories" to (calories ?: 0.0) // Asegurar un valor predeterminado para evitar nulos
        )
    }

    suspend fun saveToFirestore(repository: FirestoreRepository) {
        try {
            val ingredientId = if (id.isBlank()) repository.generateNewId("ingredients") else id
            repository.saveIngredient(ingredientId, toMap())
        } catch (e: Exception) {
            throw Exception("Error al guardar el ingrediente en Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun updateInFirestore(repository: FirestoreRepository, updates: Map<String, Any>) {
        try {
            if (id.isBlank()) throw IllegalArgumentException("No se puede actualizar un ingrediente sin ID.")
            repository.updateIngredient(id, updates)
        } catch (e: Exception) {
            throw Exception("Error al actualizar el ingrediente en Firestore: ${e.localizedMessage}")
        }
    }

    companion object {
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
                calories = (map["calories"] as? Number)?.toDouble()
            )
        }
    }
}
