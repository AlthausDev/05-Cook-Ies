package com.althaus.dev.cookIes.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.model.UserProfile
import com.althaus.dev.cookIes.ui.components.RecipeCard
import com.althaus.dev.cookIes.ui.components.SharedErrorMessage
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
//import com.althaus.dev.cookIes.ui.components.RecipeListView
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    profileViewModel: ProfileViewModel,
    onSettings: () -> Unit,
    navigateToFavorites: () -> Unit,
    onRecipeClick: (String) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        profileViewModel.clearError()
    }

    val userProfile = profileViewModel.userProfile.collectAsState()
    val userRecipes = profileViewModel.userRecipes.collectAsState()
    val isLoading = profileViewModel.isLoading.collectAsState()
    val errorMessage = profileViewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = "Mi Perfil",
                actions = {
                    IconButton(onClick = navigateToFavorites) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favoritos",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Editar Perfil",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Cabecera del perfil
                UserProfileHeader(userProfile = userProfile.value)

                Spacer(modifier = Modifier.height(16.dp))

                // Indicador de carga o mensaje de error
                when {
                    isLoading.value -> SharedLoadingIndicator()
                    errorMessage.value != null -> SharedErrorMessage(message = errorMessage.value ?: "Error desconocido")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sección de recetas del usuario
                UserRecipesSection(
                    recipes = userRecipes.value,
                    onRecipeClick = onRecipeClick
                )
            }
        }
    )
}

@Composable
fun UserProfileHeader(userProfile: UserProfile?) {
    userProfile?.let { profile ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profileImage.isNullOrBlank()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo), // Imagen predeterminada
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    // Cargar imagen desde URL
                    AsyncImage(
                        model = profile.profileImage,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = profile.name.ifBlank { "Nombre del Usuario" },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = profile.email.ifBlank { "correo@ejemplo.com" },
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Perfil no disponible",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun UserRecipesSection(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit
) {
    if (recipes.isNotEmpty()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(recipes, key = { it.id ?: "" }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    onClick = { recipe.id?.let(onRecipeClick) }
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tienes recetas aún.",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
