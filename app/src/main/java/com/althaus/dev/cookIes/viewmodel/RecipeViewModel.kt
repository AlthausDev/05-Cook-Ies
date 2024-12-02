package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.model.NotificationType
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI para manejar recetas
data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val favorites: List<Recipe> = emptyList(),
    val selectedRecipe: Recipe? = null,
    val userRatings: Map<String, Double> = emptyMap(), // Calificaciones del usuario por receta
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel que maneja la lógica de negocio para recetas, favoritos y notificaciones.
 *
 * Este ViewModel interactúa con Firestore mediante [FirestoreRepository] para realizar operaciones como
 * cargar recetas, agregar a favoritos, eliminar favoritos y calificar recetas.
 */
@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    /**
     * Estado de la UI que contiene información sobre las recetas, favoritos,
     * la receta seleccionada, calificaciones del usuario y estado de carga.
     */
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    /**
     * Flujo compartido que emite mensajes de error para ser observados externamente.
     */
    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()


    init {
        refreshRecipes()
        refreshFavorites()
    }

    // ---- Funciones principales ----

    /**
     * Carga todas las recetas desde Firestore y actualiza el estado de la UI.
     *
     * Este método obtiene una lista de todas las recetas disponibles en la base de datos,
     * las convierte en objetos [Recipe] y las almacena en el estado local ([uiState]).
     * En caso de error, emite un mensaje de error.
     */
    // Refrescar todas las recetas
    fun refreshRecipes() = viewModelScope.launch {
        try {
            _uiState.update { it.copy(isLoading = true) }
            val recipes = repository.getAllRecipes().mapNotNull { Recipe.fromMap(it) }
            updateRecipes(recipes)
        } catch (e: Exception) {
            showError("Error al cargar recetas: ${e.localizedMessage}")
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Agrega una receta a la lista de favoritos del usuario autenticado.
     *
     * Este método actualiza la lista de favoritos en Firestore y en el estado local ([uiState]).
     * También genera una notificación para el autor de la receta.
     *
     * @param recipeId ID único de la receta que se desea agregar a favoritos.
     * @throws Exception Si ocurre un error durante la actualización o si el autor no puede identificarse.
     */
    fun addToFavorites(recipeId: String) = viewModelScope.launch {
        try {
            val currentUserId = getCurrentUserId()
            val userData = repository.getUser(currentUserId)
            val updatedFavorites = userData?.get("favorites") as? List<String> ?: emptyList()

            // Verificar si ya está en favoritos
            if (!updatedFavorites.contains(recipeId)) {
                // Actualizar favoritos en Firestore
                val newFavorites = updatedFavorites + recipeId
                repository.updateUser(currentUserId, mapOf("favorites" to newFavorites))

                // Refrescar favoritos localmente
                _uiState.update {
                    it.copy(favorites = it.favorites + Recipe(id = recipeId)) // Agregar receta al estado local
                }

                // Obtener el autor de la receta
                val recipeData = repository.getRecipeOnce(recipeId)
                val authorId = recipeData?.get("authorId") as? String
                    ?: throw Exception("No se encontró el autor de la receta")

                // Crear y guardar la notificación
                val notification = Notification(
                    id = repository.generateNewId("notifications"),
                    title = "¡Tu receta ha sido marcada como favorita!",
                    message = "Un usuario ha añadido tu receta a sus favoritos.",
                    type = NotificationType.FAVORITE.toString(),
                    recipientId = authorId,
                    relatedRecipeId = recipeId
                )
                repository.saveNotification(notification)

                // Refrescar favoritos localmente
                refreshFavorites()
            }
        } catch (e: Exception) {
            showError("Error al agregar a favoritos: ${e.localizedMessage}")
        }
    }

    /**
     * Elimina una receta de la lista de favoritos del usuario autenticado.
     *
     * Este método actualiza Firestore y el estado local ([uiState]) eliminando la receta
     * especificada de la lista de favoritos. Si ocurre un error, emite un mensaje de error.
     *
     * @param recipeId ID único de la receta que se desea eliminar de favoritos.
     */
    fun removeFromFavorites(recipeId: String) = viewModelScope.launch {
        try {
            val currentUserId = getCurrentUserId()
            val userData = repository.getUser(currentUserId)
            val updatedFavorites = userData?.get("favorites") as? List<String> ?: emptyList()
            if (updatedFavorites.contains(recipeId)) {
                val newFavorites = updatedFavorites - recipeId
                repository.updateUser(currentUserId, mapOf("favorites" to newFavorites))

                // Refrescar favoritos localmente
                _uiState.update {
                    it.copy(favorites = it.favorites.filterNot { it.id == recipeId }) // Remover receta del estado local
                }

                refreshFavorites() // Refrescar la lista de favoritos
            }
        } catch (e: Exception) {
            showError("Error al eliminar de favoritos: ${e.localizedMessage}")
        }
    }

    /**
     * Refresca la lista de recetas favoritas del usuario desde Firestore.
     */
    fun refreshFavorites() = viewModelScope.launch {
        try {
            val currentUserId = getCurrentUserId()
            val userData = repository.getUser(currentUserId)
            val favoriteRecipeIds = userData?.get("favorites") as? List<String> ?: emptyList()
            val favoriteRecipes = favoriteRecipeIds.mapNotNull { id ->
                val recipeData = repository.getRecipeOnce(id)
                recipeData?.let { Recipe.fromMap(it) }
            }
            updateFavorites(favoriteRecipes)
        } catch (e: Exception) {
            showError("Error al cargar recetas favoritas: ${e.localizedMessage}")
        }
    }

    /**
     * Califica una receta con un valor proporcionado por el usuario.
     *
     * Este método actualiza la calificación de la receta tanto en Firestore como en el estado local ([uiState]).
     * También refresca la lista de recetas para reflejar las calificaciones actualizadas.
     *
     * @param recipeId ID único de la receta que se desea calificar.
     * @param rating Calificación otorgada a la receta (normalmente entre 1.0 y 5.0).
     * @throws Exception Si ocurre un error al guardar la calificación o al sincronizar los datos.
     */
    fun rateRecipe(recipeId: String, rating: Double) = viewModelScope.launch {
        try {
            val currentUserId = getCurrentUserId()

            // Obtener calificaciones actuales del usuario desde Firestore
            val userData = repository.getUserSync(currentUserId)
            val updatedRatings = userData?.ratings.orEmpty().toMutableMap().apply {
                this[recipeId] = rating // Actualizar o agregar la nueva calificación
            }

            try {
                repository.updateRecipeRating(recipeId, rating)
                refreshRecipes()
            } catch (e: Exception) {
                showError("Error al calificar la receta: ${e.localizedMessage}")
            }

            // Guardar el mapa actualizado en Firestore
            repository.updateUserRatings(currentUserId, updatedRatings)

            // Actualizar el estado local de las calificaciones
            _uiState.update { uiState ->
                uiState.copy(userRatings = updatedRatings)
            }

            println("Calificación del usuario actualizada exitosamente en Firestore.")

        } catch (e: Exception) {
            println("Error al calificar la receta: ${e.localizedMessage}")
            showError("Error al calificar la receta: ${e.localizedMessage}")
        }
    }

    /**
     * Obtiene los detalles de una receta específica por su ID.
     *
     * Este método consulta Firestore para obtener los datos de la receta, los convierte a un objeto [Recipe],
     * y actualiza el estado de la UI con la receta seleccionada. Si la receta tiene una calificación
     * del usuario, esta también se incluye en el estado.
     *
     * @param recipeId ID único de la receta que se desea obtener.
     * @throws Exception Si ocurre un error al consultar Firestore.
     */
    fun getRecipeById(recipeId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, selectedRecipe = null) }
        try {
            repository.getRecipe(recipeId).collect { data ->
                val recipe = data?.let { Recipe.fromMap(it) }
                if (recipe != null) {
                    val userRating = getUserRatingForRecipe(recipe.id) // Obtener calificación personalizada
                    _uiState.update {
                        it.copy(
                            selectedRecipe = recipe,
                            userRatings = it.userRatings.toMutableMap().apply {
                                put(recipe.id, userRating ?: 0.0)
                            },
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        } catch (e: Exception) {
            showError("Error al obtener receta: ${e.localizedMessage}")
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Actualiza el estado de la UI con una nueva lista de recetas.
     *
     * Este método actualiza la propiedad `recipes` del estado actual, reemplazándola con
     * la lista proporcionada y eliminando cualquier mensaje de error previo.
     *
     * @param recipes Lista de [Recipe] que se asignará al estado de la UI.
     */
    private fun updateRecipes(recipes: List<Recipe>) {
        _uiState.update { it.copy(recipes = recipes, errorMessage = null) }
    }

    /**
     * Actualiza el estado de la UI con una nueva lista de recetas favoritas.
     *
     * Este método actualiza la propiedad `favorites` del estado actual, reemplazándola con
     * la lista proporcionada y eliminando cualquier mensaje de error previo.
     *
     * @param favorites Lista de [Recipe] favoritas que se asignará al estado de la UI.
     */
    private fun updateFavorites(favorites: List<Recipe>) {
        _uiState.update { it.copy(favorites = favorites, errorMessage = null) }
    }

    /**
     * Establece un mensaje de error en el estado de la UI y emite el mensaje a través del flujo de errores.
     *
     * Este método actualiza la propiedad `errorMessage` del estado de la UI con el mensaje
     * proporcionado y también emite el mensaje mediante [_errorMessages] para que pueda ser
     * observado externamente.
     *
     * @param message Mensaje de error que se asignará al estado de la UI y será emitido.
     */
    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
        viewModelScope.launch {
            _errorMessages.emit(message)
        }
    }

    /**
     * Obtiene el ID del usuario autenticado actualmente.
     *
     * Este método accede al usuario autenticado a través de FirebaseAuth y retorna su
     * identificador único (UID). Si no hay un usuario autenticado, lanza una excepción.
     *
     * @return El UID del usuario autenticado actualmente.
     * @throws IllegalStateException Si no hay un usuario autenticado.
     */
    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: throw IllegalStateException("No se encontró un usuario autenticado")
    }

    /**
     * Obtiene la calificación que el usuario autenticado ha asignado a una receta específica.
     *
     * Este método consulta las calificaciones almacenadas para el usuario autenticado en Firestore
     * y retorna la calificación correspondiente al ID de la receta proporcionado, si existe.
     *
     * @param recipeId ID de la receta para la cual se busca la calificación.
     * @return La calificación asignada a la receta, o `null` si no se ha calificado.
     * @throws IllegalStateException Si no hay un usuario autenticado.
     */
    suspend fun getUserRatingForRecipe(recipeId: String): Double? {
        val currentUserId = getCurrentUserId()
        val userData = repository.getUserSync(currentUserId) // Método síncrono
        val ratings = userData?.ratings ?: return null
        return ratings[recipeId]
    }

}
