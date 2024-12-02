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
import com.althaus.dev.cookIes.theme.GradientBackground
import com.althaus.dev.cookIes.ui.components.RecipeCard
import com.althaus.dev.cookIes.ui.components.SharedErrorMessage
import com.althaus.dev.cookIes.ui.components.SharedFloatingActionButton
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

/**
 * Composable principal que representa la vista del panel de control (Dashboard).
 *
 * @param recipeViewModel [RecipeViewModel] para gestionar el estado y la lógica de las recetas.
 * @param navigateToRecipeDetail Navegación a la vista de detalles de una receta específica. Se proporciona el ID de la receta como parámetro.
 * @param navigateToProfile Navegación a la vista de perfil del usuario.
 * @param navigateToNotifications Navegación a la vista de notificaciones.
 * @param navigateToRecipeWizard Navegación al asistente para crear recetas.
 */
@Composable
fun DashboardView(
    recipeViewModel: RecipeViewModel,
    navigateToRecipeDetail: (String) -> Unit,
    navigateToProfile: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToRecipeWizard: () -> Unit
)
 {
    val uiState by recipeViewModel.uiState.collectAsState()

    // Refrescar recetas al cargar la vista
    LaunchedEffect(Unit) {
        recipeViewModel.refreshRecipes()
    }

    GradientBackground {
        Scaffold(
            topBar = {
                /**
                 * Barra superior compartida que incluye un título y dos iconos:
                 * - Icono de notificaciones: Permite navegar a la vista de notificaciones.
                 * - Imagen de perfil: Permite navegar a la vista de perfil del usuario.
                 */
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
                /**
                 * Botón flotante de acción (FAB) que permite al usuario crear una nueva receta.
                 *
                 * Utiliza el icono de "Añadir" ([Icons.Default.Add]) y llama a `navigateToRecipeWizard` al hacer clic.
                 */
                SharedFloatingActionButton(
                    onClick = navigateToRecipeWizard,
                    icon = Icons.Default.Add
                )

            },
            /**
             * Cuerpo del Dashboard que muestra diferentes estados dependiendo del estado de `uiState`:
             *
             * - Cargando: Se muestra un indicador de carga.
             * - Error: Se muestra un mensaje de error usando [SharedErrorMessage].
             * - Lista de recetas: Se renderiza una lista de recetas disponibles con [LazyColumn].
             * - Sin recetas: Se muestra un mensaje indicando que no hay recetas disponibles.
             */
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    when {
                        uiState.isLoading -> {
                            /**
                             * Indicador de carga ([SharedLoadingIndicator]) que se muestra cuando `uiState.isLoading` es verdadero.
                             */
                            SharedLoadingIndicator()

                        }
                        uiState.errorMessage != null -> {
                            /**
                             * Mensaje de error ([SharedErrorMessage]) que se muestra si `uiState.errorMessage` no es nulo.
                             *
                             * @param uiState.errorMessage Mensaje de error obtenido del ViewModel.
                             */
                            SharedErrorMessage(
                                message = uiState.errorMessage ?: "Error desconocido"
                            )

                        }
                        uiState.recipes.isNotEmpty() -> {
                            /**
                             * Lista perezosa ([LazyColumn]) que muestra las recetas disponibles.
                             *
                             * - Cada receta se representa con un [RecipeCard].
                             * - Al hacer clic en una receta, se navega a su vista de detalles utilizando `navigateToRecipeDetail`.
                             *
                             * @param uiState.recipes Lista de recetas obtenida del ViewModel.
                             */
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.recipes, key = { it.id ?: it.hashCode() }) { recipe ->
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
                                /**
                                 * Mensaje centrado indicando que no hay recetas disponibles si `uiState.recipes` está vacío.
                                 */
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
}
