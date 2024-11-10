// RecipeViewModel.kt
package com.althaus.dev.cookIes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Estado de la UI para manejar recetas
data class RecipeUiState(
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    // Estado de la UI usando RecipeUiState
    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // Flujo para errores, sin almacenar el último valor
    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()

    init {
        loadRecipes()
    }

    // Cargar recetas desde el repositorio
    fun loadRecipes() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                repository.getRecipes().collect { recipeList ->
                    _uiState.update { it.copy(recipes = recipeList, isLoading = false, errorMessage = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al cargar recetas") }
                _errorMessages.emit("Error al cargar recetas: ${e.message}")
            }
        }
    }

    // Agregar receta y actualizar estado
    fun addRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = repository.addRecipe(recipe)
            if (success) {
                loadRecipes()
            } else {
                _errorMessages.emit("Error al agregar receta")
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // Eliminar receta y actualizar estado
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val success = repository.deleteRecipe(recipeId)
            if (success) {
                loadRecipes()
            } else {
                _errorMessages.emit("Error al eliminar receta")
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
