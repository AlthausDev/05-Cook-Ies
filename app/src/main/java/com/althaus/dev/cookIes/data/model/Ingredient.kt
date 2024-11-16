package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Ingredient(
    @DocumentId val id: String = "",
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "unidad",
    val description: String = "",
    val isAllergen: Boolean = false,
    val substitutes: List<String> = emptyList(),
    val category: String = "", // Ejemplo: "Especias", "Frutas"
    val calories: Double? = null // Calorías por cantidad
) {
    init {
        require(name.isNotBlank()) { "El nombre del ingrediente no puede estar vacío." }
        require(quantity >= 0) { "La cantidad no puede ser negativa." }
    }

    fun toMap(): Map<String, Any?> {
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
        fun fromMap(map: Map<String, Any?>): Ingredient {
            return Ingredient(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                quantity = (map["quantity"] as? Number)?.toDouble() ?: 1.0,
                unit = map["unit"] as? String ?: "unidad",
                description = map["description"] as? String ?: "",
                isAllergen = map["isAllergen"] as? Boolean ?: false,
                substitutes = map["substitutes"] as? List<String> ?: emptyList(),
                category = map["category"] as? String ?: "",
                calories = (map["calories"] as? Number)?.toDouble()
            )
        }
    }
}
