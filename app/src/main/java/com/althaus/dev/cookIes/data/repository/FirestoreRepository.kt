package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val usersCollection = db.collection("users")

    // ---- Métodos de Recetas ----


    // Obtener todas las recetas
    suspend fun getAllRecipes(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("recipes").get().await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            throw Exception("Error al obtener recetas: ${e.localizedMessage}")
        }
    }

    // Obtener recetas de un usuario específico
    suspend fun getUserRecipes(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("recipes")
                .whereEqualTo("authorId", userId)
                .get().await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            throw Exception("Error al obtener recetas del usuario: ${e.localizedMessage}")
        }
    }

    // Obtener una receta por su ID (tiempo real)
    fun getRecipe(recipeId: String): Flow<Map<String, Any>?> {
        return callbackFlow {
            val documentRef = db.collection("recipes").document(recipeId)
            val listener = documentRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.data) // Mapa devuelto por Firestore
                } else {
                    trySend(null) // Documento no encontrado
                }
            }
            awaitClose { listener.remove() }
        }
    }



    // Guardar o actualizar una receta
    suspend fun saveRecipe(recipeId: String, recipeData: Map<String, Any>) {
        try {
            val finalId = if (recipeId.isBlank()) generateNewId("recipes") else recipeId
            val updatedData = recipeData.toMutableMap().apply { put("id", finalId) } // Agregar o actualizar el campo "id"
            db.collection("recipes").document(finalId).set(updatedData).await()
        } catch (e: Exception) {
            throw Exception("Error al guardar la receta: ${e.localizedMessage}")
        }
    }

    // Eliminar una receta
    suspend fun deleteRecipe(recipeId: String) {
        try {
            db.collection("recipes").document(recipeId).delete().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar la receta: ${e.localizedMessage}")
        }
    }

    // Generar un nuevo ID único para una colección
    fun generateNewId(collection: String): String {
        return db.collection(collection).document().id
    }

    // ---- Métodos de Usuarios ----

    // Guardar un usuario
    suspend fun saveUser(userId: String, name: String, email: String, profileImage: String?) {
        try {
            val user = mapOf(
                "id" to userId,
                "name" to name,
                "email" to email,
                "profileImage" to profileImage
            )
            usersCollection.document(userId).set(user).await()
        } catch (e: Exception) {
            throw Exception("Error al guardar el usuario: ${e.localizedMessage}")
        }
    }

    // Actualizar un usuario
    suspend fun updateUser(userId: String, updates: Map<String, Any>) {
        try {
            usersCollection.document(userId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar el usuario: ${e.localizedMessage}")
        }
    }

    // Obtener un usuario por su ID
    suspend fun getUser(userId: String): Map<String, Any>? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) document.data else null
        } catch (e: Exception) {
            throw Exception("Error al obtener el usuario: ${e.localizedMessage}")
        }
    }

    // Eliminar un usuario
    suspend fun deleteUser(userId: String) {
        try {
            usersCollection.document(userId).delete().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar el usuario: ${e.localizedMessage}")
        }
    }

    // ---- Métodos de Ingredientes ----

    suspend fun saveIngredient(ingredientId: String, ingredientData: Map<String, Any>) {
        try {
            db.collection("ingredients").document(ingredientId).set(ingredientData).await()
        } catch (e: Exception) {
            throw Exception("Error al guardar el ingrediente: ${e.localizedMessage}")
        }
    }

    suspend fun updateIngredient(ingredientId: String, updates: Map<String, Any>) {
        try {
            db.collection("ingredients").document(ingredientId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar el ingrediente: ${e.localizedMessage}")
        }
    }

    suspend fun getAllIngredients(): List<Map<String, Any>> {
        return try {
            val snapshot = db.collection("ingredients").get().await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            throw Exception("Error al obtener ingredientes: ${e.localizedMessage}")
        }
    }

    suspend fun getIngredient(ingredientId: String): Map<String, Any>? {
        return try {
            val snapshot = db.collection("ingredients").document(ingredientId).get().await()
            snapshot.data
        } catch (e: Exception) {
            throw Exception("Error al obtener el ingrediente: ${e.localizedMessage}")
        }
    }

    suspend fun deleteIngredient(ingredientId: String) {
        try {
            db.collection("ingredients").document(ingredientId).delete().await()
        } catch (e: Exception) {
            throw Exception("Error al eliminar el ingrediente: ${e.localizedMessage}")
        }
    }

    // ---- Métodos de Notificaciones ----

    suspend fun saveNotification(notificationId: String, notificationData: Map<String, Any>) {
        try {
            db.collection("notifications").document(notificationId).set(notificationData).await()
        } catch (e: Exception) {
            throw Exception("Error al guardar la notificación: ${e.localizedMessage}")
        }
    }

    suspend fun updateNotification(notificationId: String, updates: Map<String, Any>) {
        try {
            db.collection("notifications").document(notificationId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar la notificación: ${e.localizedMessage}")
        }
    }

    suspend fun getNotifications(recipientId: String): List<Notification> {
        return try {
            val snapshot = db.collection("notifications")
                .whereEqualTo("recipientId", recipientId)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            throw Exception("Error al obtener notificaciones: ${e.localizedMessage}")
        }
    }
}
