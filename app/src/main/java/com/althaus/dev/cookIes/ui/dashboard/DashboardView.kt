package com.althaus.dev.cookIes.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.ui.components.RecipeCard
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

    // Cargar o refrescar recetas cuando la vista se inicializa
    LaunchedEffect(Unit) {
        recipeViewModel.refreshRecipes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp), // Aseguramos márgenes generales
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Título en el lado izquierdo
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge
                        )

                        // Logo e ícono de notificaciones juntos
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = navigateToNotifications) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp) // Tamaño total del logo
                                    .clickable(onClick = navigateToProfile) // Hacer clic en el logo
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Imagen de Perfil",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                        }
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToRecipeWizard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar receta",
                    modifier = Modifier.size(24.dp)
                )
            }
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
                        // Indicador de carga si `isLoading` está activo
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.errorMessage != null -> {
                        // Mostrar mensaje de error si hay algún error en el `uiState`
                        Text(
                            text = uiState.errorMessage ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    uiState.recipes.isNotEmpty() -> {
                        // Mostrar lista de recetas si `recipes` no está vacío
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.recipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { navigateToRecipeDetail(recipe.id ?: "") }
                                )
                            }
                        }
                    }
                    else -> {
                        // Mensaje si no hay recetas disponibles
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay recetas disponibles")
                        }
                    }
                }
            }
        }
    )
}
