package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    // ---- Método genérico para guardar o actualizar en cualquier colección ----
    private suspend fun saveToCollection(collection: String, documentId: String, data: Map<String, Any>) {
        db.collection(collection).document(documentId).set(data).await()
    }

    // ---- Método genérico para actualizar campos existentes ----
    private suspend fun updateToCollection(collection: String, documentId: String, updates: Map<String, Any>) {
        db.collection(collection).document(documentId).update(updates).await()
    }

    // ---- Usuarios ----

    // ---- Usuarios ----

    suspend fun saveUser(userId: String, name: String, email: String, profileImage: String?) {
        val userMap = mapOf(
            "name" to name,
            "email" to email,
            "profileImage" to profileImage
        ).filterValues { it != null } // Filtrar valores nulos
            .mapValues { it.value!! } // Garantizar que no queden valores nulos

        saveToCollection("users", userId, userMap)
    }



    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        updateToCollection("users", userId, updates)
    }

    suspend fun getUser(userId: String): Map<String, Any>? {
        val snapshot = db.collection("users").document(userId).get().await()
        return snapshot.data
    }

    suspend fun deleteUser(userId: String) {
        db.collection("users").document(userId).delete().await()
    }

    // ---- Recetas ----

    suspend fun saveRecipe(recipeId: String, recipeData: Map<String, Any>) {
        saveToCollection("recipes", recipeId, recipeData)
    }

    suspend fun updateRecipe(recipeId: String, updates: Map<String, Any>) {
        updateToCollection("recipes", recipeId, updates)
    }

    suspend fun getUserRecipes(userId: String): List<Map<String, Any>> {
        val snapshot = db.collection("recipes").whereEqualTo("authorId", userId).get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    suspend fun getRecipe(recipeId: String): Map<String, Any>? {
        val snapshot = db.collection("recipes").document(recipeId).get().await()
        return snapshot.data
    }

    suspend fun deleteRecipe(recipeId: String) {
        db.collection("recipes").document(recipeId).delete().await()
    }

    suspend fun getAllRecipes(): List<Map<String, Any>> {
        val snapshot = db.collection("recipes").get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    // ---- Ingredientes ----

    suspend fun saveIngredient(ingredientId: String, ingredientData: Map<String, Any>) {
        saveToCollection("ingredients", ingredientId, ingredientData)
    }

    suspend fun updateIngredient(ingredientId: String, updates: Map<String, Any>) {
        updateToCollection("ingredients", ingredientId, updates)
    }

    suspend fun getAllIngredients(): List<Map<String, Any>> {
        val snapshot = db.collection("ingredients").get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    suspend fun getIngredient(ingredientId: String): Map<String, Any>? {
        val snapshot = db.collection("ingredients").document(ingredientId).get().await()
        return snapshot.data
    }

    suspend fun deleteIngredient(ingredientId: String) {
        db.collection("ingredients").document(ingredientId).delete().await()
    }

    suspend fun searchIngredientsByName(name: String): List<Map<String, Any>> {
        val snapshot = db.collection("ingredients")
            .whereEqualTo("name", name)
            .get().await()
        return snapshot.documents.map { it.data ?: emptyMap() }
    }

    // ---- Notificaciones ----

    suspend fun saveNotification(notificationId: String, notificationData: Map<String, Any>) {
        saveToCollection("notifications", notificationId, notificationData)
    }

    suspend fun updateNotification(notificationId: String, updates: Map<String, Any>) {
        updateToCollection("notifications", notificationId, updates)
    }


    suspend fun generateNewId(collection: String): String {
        return db.collection(collection).document().id
    }

}
