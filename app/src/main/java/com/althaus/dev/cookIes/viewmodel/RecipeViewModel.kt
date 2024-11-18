package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.RecipeRepository
import com.althaus.dev.cookIes.data.repository.RecipeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI para manejar recetas
data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val favorites: List<Recipe> = emptyList(), // Lista de recetas favoritas
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()

    init {
        refreshRecipes()
        refreshFavorites()
    }

    // Refrescar las recetas (cargar o recargar)
    fun refreshRecipes() = handleLoading {
        repository.getRecipes().collect { result ->
            when (result) {
                is RecipeResult.Success -> updateRecipes(result.data)
                is RecipeResult.Failure -> showError("Error al cargar recetas: ${result.exception.localizedMessage}")
            }
        }
    }

    // Refrescar las recetas favoritas
    fun refreshFavorites(userId: String = getCurrentUserId()) = handleLoading {
        when (val result = repository.getFavorites(userId)) {
            is RecipeResult.Success -> updateFavorites(result.data)
            is RecipeResult.Failure -> showError("Error al cargar recetas favoritas: ${result.exception.localizedMessage}")
        }
    }

    // Agregar receta
    fun addRecipe(recipe: Recipe) = handleLoading {
        when (val result = repository.addRecipe(recipe)) {
            is RecipeResult.Success -> refreshRecipes()
            is RecipeResult.Failure -> showError("Error al agregar receta: ${result.exception.localizedMessage}")
        }
    }

    // Eliminar receta
    fun deleteRecipe(recipeId: String) = handleLoading {
        when (val result = repository.deleteRecipe(recipeId)) {
            is RecipeResult.Success -> refreshRecipes()
            is RecipeResult.Failure -> showError("Error al eliminar receta: ${result.exception.localizedMessage}")
        }
    }

    // ---- Métodos Privados ----

    // Manejar operaciones de carga y estado
    private fun handleLoading(operation: suspend () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                operation()
            } catch (e: Exception) {
                showError("Error inesperado: ${e.localizedMessage}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Actualizar la lista de recetas
    private fun updateRecipes(recipes: List<Recipe>) {
        _uiState.update { it.copy(recipes = recipes, errorMessage = null) }
    }

    // Actualizar la lista de recetas favoritas
    private fun updateFavorites(favorites: List<Recipe>) {
        _uiState.update { it.copy(favorites = favorites, errorMessage = null) }
    }

    // Mostrar error en el flujo de mensajes y en el estado de UI
    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
        viewModelScope.launch {
            _errorMessages.emit(message)
        }
    }

    // Obtener el ID del usuario actual (puedes adaptar esta función según tu implementación)
    private fun getCurrentUserId(): String {
        // Aquí deberías implementar cómo obtienes el ID del usuario autenticado
        return "user-id-placeholder"
    }
}
