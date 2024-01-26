package com.althaus.dev.project05_recetario.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.althaus.dev.project05_recetario.repository.RecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {

    val recipes = 1
    fun loadRecipes() {
        viewModelScope.launch {
        }
    }
}