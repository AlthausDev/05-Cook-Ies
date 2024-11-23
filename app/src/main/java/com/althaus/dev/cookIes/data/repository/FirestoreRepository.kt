package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
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
    private val notificationsCollection = db.collection("notifications")
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown-user"


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



    suspend fun saveRecipe(recipeId: String, recipeData: Map<String, Any>, currentAuthorId: String) {
        try {
            // Determinar si necesitamos generar un nuevo ID
            val finalId = if (recipeId.isBlank()) generateNewId("recipes") else recipeId

            // Preparar los datos con el ID y el autor
            val updatedData = recipeData.toMutableMap().apply {
                put("id", finalId) // Asegurar que el ID está incluido en los datos
                if (!containsKey("authorId")) {
                    put("authorId", currentAuthorId) // Agregar el authorId si no está presente
                }
            }

            // Guardar en Firestore
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

    suspend fun updateRecipe(recipeId: String, updates: Map<String, Any>) {
        try {
            db.collection("recipes").document(recipeId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar la receta: ${e.localizedMessage}")
        }
    }

    suspend fun updateRecipeRating(recipeId: String, newRating: Double) {
        try {
            val recipeData = getRecipeOnce(recipeId) ?: throw Exception("Receta no encontrada")
            val currentRating = (recipeData["averageRating"] as? Number)?.toFloat() ?: 0.0f
            val currentCount = (recipeData["ratingCount"] as? Number)?.toInt() ?: 0

            val updatedRating = ((currentRating * currentCount) + newRating) / (currentCount + 1)
            val updatedCount = currentCount + 1

            updateRecipe(recipeId, mapOf("averageRating" to updatedRating, "ratingCount" to updatedCount))
        } catch (e: Exception) {
            throw Exception("Error al actualizar el rating de la receta: ${e.localizedMessage}")
        }
    }

    suspend fun updateUserRatings(userId: String, ratings: Map<String, Double>) {
        try {
            println("Actualizando ratings para el usuario $userId con valores: $ratings")

            val userDocument = usersCollection.document(userId)
            val snapshot = userDocument.get().await()

            if (snapshot.exists()) {
                val existingRatings = snapshot.get("ratings") as? Map<String, Double> ?: emptyMap()
                println("Ratings existentes: $existingRatings")

                val updatedRatings = existingRatings.toMutableMap().apply {
                    putAll(ratings)
                }
                userDocument.update("ratings", updatedRatings).await()
                println("Ratings actualizados correctamente en Firestore para el usuario $userId")
            } else {
                userDocument.set(mapOf("ratings" to ratings)).await()
                println("Se creó el campo ratings y se actualizó en Firestore para el usuario $userId")
            }
        } catch (e: Exception) {
            println("Error al actualizar ratings en Firestore: ${e.localizedMessage}")
            throw Exception("Error al actualizar las calificaciones del usuario: ${e.localizedMessage}")
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

    /**
     * Guarda una nueva notificación en Firestore.
     */
    suspend fun saveNotification(notification: Notification) {
        try {
            db.collection("notifications").document(notification.id).set(notification).await()
        } catch (e: Exception) {
            throw Exception("Error al guardar la notificación: ${e.localizedMessage}")
        }
    }

    /**
     * Actualiza una notificación existente en Firestore.
     */
    suspend fun updateNotification(notificationId: String, updates: Map<String, Any>) {
        try {
            notificationsCollection.document(notificationId).update(updates).await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar la notificación: ${e.localizedMessage}")
        }
    }

    /**
     * Obtiene todas las notificaciones para un usuario específico.
     */
    suspend fun getNotifications(recipientId: String): List<Notification> {
        return try {
            val snapshot = db.collection("notifications")
                .whereEqualTo("recipientId", recipientId)
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Registro detallado del error
            throw Exception("Error al obtener notificaciones: ${e.localizedMessage}")
        }
    }


    /**
     * Marca una notificación como leída.
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        try {
            updateNotification(notificationId, mapOf("isRead" to true))
        } catch (e: Exception) {
            throw Exception("Error al marcar la notificación como leída: ${e.localizedMessage}")
        }
    }

    /**
     * Obtiene una notificación específica por ID.
     */
    suspend fun getNotification(notificationId: String): Notification? {
        return try {
            val snapshot = notificationsCollection.document(notificationId).get().await()
            snapshot.toObject(Notification::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            throw Exception("Error al obtener la notificación: ${e.localizedMessage}")
        }
    }

    suspend fun getUserSync(userId: String): UserProfile? {
        return try {
            val userData = getUser(userId)
            userData?.let { UserProfile.fromMap(it) }
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getRecipeOnce(recipeId: String): Map<String, Any>? {
        return try {
            val snapshot = db.collection("recipes").document(recipeId).get().await()
            snapshot.data
        } catch (e: Exception) {
            throw Exception("Error al obtener receta: ${e.localizedMessage}")
        }
    }

}
