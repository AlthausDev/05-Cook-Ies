package com.althaus.dev.cookIes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.data.repository.RecipeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
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

    // Refrescar las recetas (cargar o recargar)
    fun refreshRecipes() = handleLoading {
        try {
            val recipes = repository.getAllRecipes().mapNotNull { data ->
                Recipe.fromMap(data)  // Asumiendo que tienes un método para convertir Map a Recipe
            }
            updateRecipes(recipes)
        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Error al recolectar recetas: ${e.localizedMessage}", e)
            showError("Error al recolectar recetas: ${e.localizedMessage}")
        }
    }

    // Refrescar las recetas favoritas
    fun refreshFavorites(userId: String = getCurrentUserId()) = handleLoading {
        try {
            val favorites = repository.getUserRecipes(userId).mapNotNull { data ->
                Recipe.fromMap(data)  // Asumiendo que tienes un método para convertir Map a Recipe
            }
            updateFavorites(favorites)
        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Error al recolectar recetas favoritas: ${e.localizedMessage}", e)
            showError("Error al recolectar recetas favoritas: ${e.localizedMessage}")
        }
    }

    // Agregar receta
    fun addRecipe(recipe: Recipe) = handleLoading {
        try {
            val recipeData = recipe.toMap() // Asumiendo que tienes un método para convertir Recipe a Map
            repository.saveRecipe(recipe.id ?: repository.generateNewId("recipes"), recipeData)
            Log.d("RecipeViewModel", "Receta agregada exitosamente")
            refreshRecipes()
        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Error inesperado al agregar receta: ${e.localizedMessage}", e)
            showError("Error inesperado al agregar receta: ${e.localizedMessage}")
        }
    }

    // Eliminar receta
    fun deleteRecipe(recipeId: String) = handleLoading {
        try {
            repository.deleteRecipe(recipeId)
            Log.d("RecipeViewModel", "Receta eliminada exitosamente")
            refreshRecipes()
        } catch (e: Exception) {
            Log.e("RecipeViewModel", "Error inesperado al eliminar receta: ${e.localizedMessage}", e)
            showError("Error inesperado al eliminar receta: ${e.localizedMessage}")
        }
    }

    // ---- Métodos Privados ----

    // Manejar operaciones de carga y estado
    private fun handleLoading(operation: suspend () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                operation()
            } catch (e: IllegalArgumentException) {
                Log.e("RecipeViewModel", "Argumento no válido: ${e.localizedMessage}", e)
                showError("Argumento no válido: ${e.localizedMessage}")
            } catch (e: InvocationTargetException) {
                Log.e("RecipeViewModel", "Error de reflexión: ${e.localizedMessage}", e)
                showError("Error de reflexión: ${e.localizedMessage}")
                e.cause?.printStackTrace()
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error inesperado: ${e.localizedMessage}", e)
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
