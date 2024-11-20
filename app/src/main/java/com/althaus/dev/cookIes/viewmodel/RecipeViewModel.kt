package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            if (!updatedFavorites.contains(recipeId)) {
                val newFavorites = updatedFavorites + recipeId
                repository.updateUser(currentUserId, mapOf("favorites" to newFavorites))
                refreshFavorites() // Refrescar la lista de favoritos
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



    // Refrescar las recetas favoritas
//    fun refreshFavorites() = viewModelScope.launch {
//        try {
//            val userId = getCurrentUserId()
//            _uiState.update { it.copy(isLoading = true) }
//            val favorites = repository.getUserRecipes(userId).mapNotNull { Recipe.fromMap(it) }
//            updateFavorites(favorites)
//        } catch (e: Exception) {
//            showError("Error al cargar recetas favoritas: ${e.localizedMessage}")
//        } finally {
//            _uiState.update { it.copy(isLoading = false) }
//        }
//    }




    // Obtener una receta por su ID
    fun getRecipeById(recipeId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, selectedRecipe = null) }
        try {
            repository.getRecipe(recipeId).collect { data ->
                val recipe = data?.let { Recipe.fromMap(it) } // Convierte el mapa en un objeto Recipe
                _uiState.update { it.copy(selectedRecipe = recipe, isLoading = false) }
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

}
