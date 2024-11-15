package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class RecipeResult<out T> {
    data class Success<out T>(val data: T) : RecipeResult<T>()
    data class Failure(val exception: Exception) : RecipeResult<Nothing>()
}

class RecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val recipesCollection = firestore.collection("recipes")

    // Método auxiliar para manejar errores en llamadas de Firestore
    private suspend fun <T> safeRecipeCall(call: suspend () -> T): RecipeResult<T> {
        return try {
            RecipeResult.Success(call())
        } catch (e: Exception) {
            RecipeResult.Failure(e)
        }
    }

    // Obtener todas las recetas como Flow
    suspend fun getRecipes(): Flow<RecipeResult<List<Recipe>>> = flow {
        emit(safeRecipeCall {
            val snapshot = recipesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
        })
    }

    // Obtener recetas de un usuario específico
    suspend fun getUserRecipes(userId: String): Flow<RecipeResult<List<Recipe>>> = flow {
        emit(safeRecipeCall {
            val snapshot = recipesCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
        })
    }

    // Obtener una receta específica por ID
    suspend fun getRecipeById(recipeId: String): Flow<RecipeResult<Recipe?>> = flow {
        emit(safeRecipeCall {
            recipesCollection.document(recipeId).get().await().toObject(Recipe::class.java)
        })
    }

    // Agregar una nueva receta
    suspend fun addRecipe(recipe: Recipe): RecipeResult<Boolean> = safeRecipeCall {
        val newDocRef = recipesCollection.document()
        recipesCollection.document(newDocRef.id).set(recipe.copy(id = newDocRef.id)).await()
        true
    }

    // Actualizar una receta existente
    suspend fun updateRecipe(recipe: Recipe): RecipeResult<Boolean> = safeRecipeCall {
        recipe.id?.let {
            recipesCollection.document(it).set(recipe).await()
            true
        } ?: throw IllegalArgumentException("Recipe ID cannot be null")
    }

    // Eliminar una receta por ID
    suspend fun deleteRecipe(recipeId: String): RecipeResult<Boolean> = safeRecipeCall {
        recipesCollection.document(recipeId).delete().await()
        true
    }
}
