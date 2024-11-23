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

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()

    init {
        refreshRecipes()
        refreshFavorites()
    }

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

                // Obtener el autor de la receta
                val recipeData = repository.getRecipeOnce(recipeId)
                val authorId = recipeData?.get("authorId") as? String
                    ?: throw Exception("No se encontró el autor de la receta")

                // Crear y guardar la notificación
                val notification = Notification(
                    id = repository.generateNewId("notifications"), // Generar un ID único
                    title = "¡Tu receta ha sido marcada como favorita!",
                    message = "Un usuario ha añadido tu receta a sus favoritos.",
                    type = NotificationType.FAVORITE,
                    recipientId = authorId,
                    relatedRecipeId = recipeId
                )
                repository.saveNotification(notification) // Usar directamente el objeto Notification

                // Refrescar favoritos localmente
                refreshFavorites()
            }
        } catch (e: Exception) {
            showError("Error al agregar a favoritos: ${e.localizedMessage}")
        }
    }

    fun removeFromFavorites(recipeId: String) = viewModelScope.launch {
        try {
            val currentUserId = getCurrentUserId()
            val userData = repository.getUser(currentUserId)
            val updatedFavorites = userData?.get("favorites") as? List<String> ?: emptyList()
            if (updatedFavorites.contains(recipeId)) {
                val newFavorites = updatedFavorites - recipeId
                repository.updateUser(currentUserId, mapOf("favorites" to newFavorites))
                refreshFavorites() // Refrescar la lista de favoritos
            }
        } catch (e: Exception) {
            showError("Error al eliminar de favoritos: ${e.localizedMessage}")
        }
    }

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


    // Obtener una receta por su ID
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

    // Manejo de estado y errores
    private fun updateRecipes(recipes: List<Recipe>) {
        _uiState.update { it.copy(recipes = recipes, errorMessage = null) }
    }

    private fun updateFavorites(favorites: List<Recipe>) {
        _uiState.update { it.copy(favorites = favorites, errorMessage = null) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
        viewModelScope.launch {
            _errorMessages.emit(message)
        }
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: throw IllegalStateException("No se encontró un usuario autenticado")
    }

    suspend fun getUserRatingForRecipe(recipeId: String): Double? {
        val currentUserId = getCurrentUserId()
        val userData = repository.getUserSync(currentUserId) // Método síncrono
        val ratings = userData?.ratings ?: return null
        return ratings[recipeId]
    }
}
