package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val recipesCollection = firestore.collection("recipes")

    // Obtener todas las recetas
    suspend fun getRecipes(): Flow<List<Recipe>> = flow {
        try {
            val snapshot = recipesCollection.get().await()
            val recipes = snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
            emit(recipes)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList()) // Emitir lista vacía en caso de error
        }
    }

    // Obtener una receta específica por ID
    suspend fun getRecipeById(recipeId: String): Flow<Recipe?> = flow {
        try {
            val snapshot = recipesCollection.document(recipeId).get().await()
            val recipe = snapshot.toObject(Recipe::class.java)
            emit(recipe)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null) // Emitir null en caso de error
        }
    }

    // Agregar una nueva receta
    suspend fun addRecipe(recipe: Recipe): Boolean {
        return try {
            val newDocRef = recipesCollection.document()
            recipesCollection.document(newDocRef.id).set(recipe.copy(id = newDocRef.id)).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Actualizar una receta existente
    suspend fun updateRecipe(recipe: Recipe): Boolean {
        return try {
            recipe.id?.let {
                recipesCollection.document(it).set(recipe).await()
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Eliminar una receta por ID
    suspend fun deleteRecipe(recipeId: String): Boolean {
        return try {
            recipesCollection.document(recipeId).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
