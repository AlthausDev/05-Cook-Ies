package com.althaus.dev.cookIes.ui.dashboard

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.ui.components.RecipeCard
import com.althaus.dev.cookIes.ui.components.SharedErrorMessage
import com.althaus.dev.cookIes.ui.components.SharedFloatingActionButton
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardView(
    recipeViewModel: RecipeViewModel,
    navigateToRecipeDetail: (String) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToRecipeWizard: () -> Unit
) {
    val uiState by recipeViewModel.uiState.collectAsState()

    // Refrescar recetas al cargar la vista
    LaunchedEffect(Unit) {
        recipeViewModel.refreshRecipes()
    }

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = "Dashboard",
                actions = {
                    IconButton(onClick = navigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(60.dp)
                            .clickable(onClick = navigateToProfile)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Imagen de Perfil",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            SharedFloatingActionButton(
                onClick = navigateToRecipeWizard,
                icon = Icons.Default.Add
            )
        },

                content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                when {
                    uiState.isLoading -> {
                        SharedLoadingIndicator()
                    }
                    uiState.errorMessage != null -> {
                        SharedErrorMessage(
                            message = uiState.errorMessage ?: "Error desconocido"
                        )
                    }
                    uiState.recipes.isNotEmpty() -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.recipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = {
                                        recipe.id?.let { recipeId ->
                                            navigateToRecipeDetail(recipeId)
                                        } ?: run {
                                            Log.e("DashboardView", "Error: ID de receta es nulo o vacío")
                                        }
                                    }
                                )
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay recetas disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    )
}
