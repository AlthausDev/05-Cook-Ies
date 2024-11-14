package com.althaus.dev.cookIes.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Ingredient(
    @DocumentId val id: String = "",             // ID del ingrediente en Firestore, si es necesario
    val name: String,                            // Nombre del ingrediente
    val quantity: Double = 1.0,                  // Cantidad del ingrediente, predeterminado a 1.0
    val unit: String = "unidad",                 // Unidad de medida (ej. "gramos", "ml"), predeterminada a "unidad"
    val description: String = "",                // Descripción o notas adicionales sobre el ingrediente
    val isAllergen: Boolean = false,             // Indicador si el ingrediente es alergénico
    val substitutes: List<String> = emptyList()  // Lista inmutable de posibles sustitutos
) {
    init {
        require(quantity >= 0) { "La cantidad no puede ser negativa." }
    }
}
