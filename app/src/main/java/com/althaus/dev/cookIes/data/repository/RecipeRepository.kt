package com.althaus.dev.cookIes.data.repository

import com.althaus.dev.cookIes.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Resultado de las operaciones relacionadas con recetas.
 *
 * Esta clase sellada representa los posibles resultados:
 * - [Success]: Operación exitosa con datos.
 * - [Failure]: Operación fallida con una excepción.
 */
sealed class RecipeResult<out T> {
    /**
     * Representa un resultado exitoso con datos asociados.
     *
     * @param data Datos devueltos por la operación.
     */
    data class Success<out T>(val data: T) : RecipeResult<T>()

    /**
     * Representa un resultado fallido con una excepción asociada.
     *
     * @param exception La excepción generada por la operación fallida.
     */
    data class Failure(val exception: Exception) : RecipeResult<Nothing>()
}

/**
 * Repositorio que maneja operaciones CRUD relacionadas con recetas en Firestore.
 *
 * @property firestore Instancia de [FirebaseFirestore] utilizada para interactuar con la base de datos.
 */
class RecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val recipesCollection = firestore.collection("recipes")

    /**
     * Método auxiliar para manejar llamadas a Firestore con manejo de errores.
     *
     * @param call Bloque de código que realiza la llamada a Firestore.
     * @return [RecipeResult] con los datos de la operación o un error si ocurre.
     */
    private suspend fun <T> safeRecipeCall(call: suspend () -> T): RecipeResult<T> {
        return try {
            RecipeResult.Success(call())
        } catch (e: Exception) {
            RecipeResult.Failure(e)
        }
    }

    /**
     * Obtiene las recetas favoritas de un usuario.
     *
     * @param userId ID del usuario cuyos favoritos se desean obtener.
     * @return [RecipeResult] con una lista de [Recipe] o un error si ocurre.
     */
    suspend fun getFavorites(userId: String): RecipeResult<List<Recipe>> {
        return safeRecipeCall {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
        }
    }

    /**
     * Obtiene todas las recetas como un flujo ([Flow]).
     *
     * @return Un flujo que emite un [RecipeResult] con una lista de [Recipe].
     */
    suspend fun getRecipes(): Flow<RecipeResult<List<Recipe>>> = flow {
        emit(safeRecipeCall {
            val snapshot = recipesCollection.get().await()
            snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
        })
    }

    /**
     * Obtiene todas las recetas de un usuario específico como un flujo ([Flow]).
     *
     * @param userId ID del usuario cuyas recetas se desean obtener.
     * @return Un flujo que emite un [RecipeResult] con una lista de [Recipe].
     */
    suspend fun getRecipesByUser(userId: String): Flow<RecipeResult<List<Recipe>>> = flow {
        emit(safeRecipeCall {
            val snapshot = recipesCollection.whereEqualTo("userId", userId).get().await()
            snapshot.documents.mapNotNull { it.toObject(Recipe::class.java) }
        })
    }

    /**
     * Obtiene una receta específica por su ID como un flujo ([Flow]).
     *
     * @param recipeId ID de la receta que se desea obtener.
     * @return Un flujo que emite un [RecipeResult] con la receta o `null` si no se encuentra.
     */
    suspend fun getRecipeById(recipeId: String): Flow<RecipeResult<Recipe?>> = flow {
        emit(safeRecipeCall {
            recipesCollection.document(recipeId).get().await().toObject(Recipe::class.java)
        })
    }

    /**
     * Agrega una nueva receta a Firestore.
     *
     * @param recipe Objeto [Recipe] que se desea agregar.
     * @return [RecipeResult] indicando si la operación fue exitosa o no.
     */
    suspend fun addRecipe(recipe: Recipe): RecipeResult<Boolean> = safeRecipeCall {
        val newDocRef = recipesCollection.document()
        recipesCollection.document(newDocRef.id).set(recipe.copy(id = newDocRef.id)).await()
        true
    }

    /**
     * Actualiza una receta existente en Firestore.
     *
     * @param recipe Objeto [Recipe] que se desea actualizar.
     * @return [RecipeResult] indicando si la operación fue exitosa o no.
     * @throws IllegalArgumentException Si el ID de la receta es `null`.
     */
    suspend fun updateRecipe(recipe: Recipe): RecipeResult<Boolean> = safeRecipeCall {
        recipe.id?.let {
            recipesCollection.document(it).set(recipe).await()
            true
        } ?: throw IllegalArgumentException("Recipe ID cannot be null")
    }

    /**
     * Elimina una receta por su ID en Firestore.
     *
     * @param recipeId ID de la receta que se desea eliminar.
     * @return [RecipeResult] indicando si la operación fue exitosa o no.
     */
    suspend fun deleteRecipe(recipeId: String): RecipeResult<Boolean> = safeRecipeCall {
        recipesCollection.document(recipeId).delete().await()
        true
    }
}
