package com.althaus.dev.cookIes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // Guardar o actualizar un usuario
    suspend fun saveUser(userId: String, name: String, email: String, profileImage: String?) {
        val userMap = mapOf(
            "name" to name,
            "email" to email,
            "profileImage" to profileImage
        )
        db.collection("users").document(userId).set(userMap).await()
    }

    // Obtener datos del usuario
    suspend fun getUser(userId: String): Map<String, Any>? {
        val snapshot = db.collection("users").document(userId).get().await()
        return snapshot.data
    }

    // Guardar una receta
    suspend fun saveRecipe(recipeId: String, recipeData: Map<String, Any>) {
        db.collection("recipes").document(recipeId).set(recipeData).await()
    }

    // Obtener todas las recetas de un usuario
    suspend fun getUserRecipes(userId: String): List<Map<String, Any>> {
        val snapshot = db.collection("recipes").whereEqualTo("authorId", userId).get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }
}
