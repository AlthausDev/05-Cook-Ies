package com.althaus.dev.cookIes.data.model

import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Representa un ingrediente en el sistema.
 *
 * Esta clase modela las propiedades y comportamientos asociados a un ingrediente,
 * incluyendo detalles como su nombre, cantidad, unidad de medida, y si es un alérgeno.
 * También incluye métodos para interactuar con Firebase Firestore.
 */
@IgnoreExtraProperties
data class Ingredient(
    @DocumentId val id: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    val unit: String = "unidad",
    val description: String = "",
    val isAllergen: Boolean = false,
    val substitutes: List<String> = emptyList(),
    val category: String = "",
    val calories: Double = 1.0
) {
    init {
        // Validaciones para evitar datos inválidos
        require(name.isNotBlank()) { "El nombre del ingrediente no puede estar vacío." }
        require(quantity >= 0) { "La cantidad no puede ser negativa." }
    }

    /**
     * Convierte el objeto Ingredient en un mapa de valores clave.
     *
     * @return Un mapa con las propiedades del ingrediente.
     */
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

    companion object {
        /**
         * Crea una instancia de Ingredient a partir de un mapa de valores clave.
         *
         * @param map Mapa con las propiedades del ingrediente.
         * @return Una instancia de Ingredient.
         */
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
