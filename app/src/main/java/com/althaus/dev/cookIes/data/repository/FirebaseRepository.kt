package com.althaus.dev.cookIes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    // ---- Usuarios ----

    // Guardar o actualizar un usuario
    suspend fun saveUser(userId: String, name: String, email: String, profileImage: String?) {
        val userMap = mapOf(
            "name" to name,
            "email" to email,
            "profileImage" to profileImage
        )
        db.collection("users").document(userId).set(userMap).await()
    }

    // Obtener datos de un usuario
    suspend fun getUser(userId: String): Map<String, Any>? {
        val snapshot = db.collection("users").document(userId).get().await()
        return snapshot.data
    }

    // Eliminar un usuario
    suspend fun deleteUser(userId: String) {
        db.collection("users").document(userId).delete().await()
    }

    // ---- Recetas ----

    // Guardar o actualizar una receta
    suspend fun saveRecipe(recipeId: String, recipeData: Map<String, Any>) {
        db.collection("recipes").document(recipeId).set(recipeData).await()
    }

    // Obtener todas las recetas de un usuario
    suspend fun getUserRecipes(userId: String): List<Map<String, Any>> {
        val snapshot = db.collection("recipes").whereEqualTo("authorId", userId).get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    // Obtener una receta por ID
    suspend fun getRecipe(recipeId: String): Map<String, Any>? {
        val snapshot = db.collection("recipes").document(recipeId).get().await()
        return snapshot.data
    }

    // Eliminar una receta
    suspend fun deleteRecipe(recipeId: String) {
        db.collection("recipes").document(recipeId).delete().await()
    }

    // Obtener todas las recetas
    suspend fun getAllRecipes(): List<Map<String, Any>> {
        val snapshot = db.collection("recipes").get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    // ---- Ingredientes ----

    // Guardar o actualizar un ingrediente
    suspend fun saveIngredient(ingredientId: String, ingredientData: Map<String, Any>) {
        db.collection("ingredients").document(ingredientId).set(ingredientData).await()
    }

    // Obtener todos los ingredientes
    suspend fun getAllIngredients(): List<Map<String, Any>> {
        val snapshot = db.collection("ingredients").get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    // Obtener un ingrediente por ID
    suspend fun getIngredient(ingredientId: String): Map<String, Any>? {
        val snapshot = db.collection("ingredients").document(ingredientId).get().await()
        return snapshot.data
    }

    // Eliminar un ingrediente
    suspend fun deleteIngredient(ingredientId: String) {
        db.collection("ingredients").document(ingredientId).delete().await()
    }

    // Obtener ingredientes por nombre
    suspend fun searchIngredientsByName(name: String): List<Map<String, Any>> {
        val snapshot = db.collection("ingredients")
            .whereEqualTo("name", name)
            .get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }
}
