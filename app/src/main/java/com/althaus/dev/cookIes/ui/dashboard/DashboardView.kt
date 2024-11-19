package com.althaus.dev.cookIes.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.PrimaryLight
import com.althaus.dev.cookIes.theme.SecondaryLight
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

    // Refrescar recetas al cargar la vista
    LaunchedEffect(Unit) {
        recipeViewModel.refreshRecipes()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryLight, SecondaryLight)
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dashboard",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = navigateToRecipeWizard,
                    containerColor = SecondaryLight,
                    contentColor = PrimaryDark,
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = PrimaryDark.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.large
                    )
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
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "Error desconocido",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        uiState.recipes.isNotEmpty() -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.recipes) { recipe ->
                                    RecipeCard(
                                        recipe = recipe,
                                        onClick = { recipe.id?.let { navigateToRecipeDetail(it) } }
                                    )
                                }
                            }
                        }
                        else -> {
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
}
