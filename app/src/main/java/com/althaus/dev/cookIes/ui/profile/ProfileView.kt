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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.PrimaryLight
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.SecondaryLight
import com.althaus.dev.cookIes.ui.components.RecipeCard
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
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mi Perfil",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(onClick = navigateToFavorites) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Favoritos",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Editar Perfil",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
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
                    .background(Brush.verticalGradient(listOf(PrimaryLight, SecondaryLight))),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar perfil de usuario
                userProfile.value?.let { profile ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = profile.name ?: "Nombre del Usuario",
                        color = PrimaryDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = profile.email ?: "correo@ejemplo.com",
                        color = PrimaryDark.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Indicador de carga o mensaje de error
                if (isLoading.value) {
                    CircularProgressIndicator(color = PrimaryDark)
                } else if (errorMessage.value != null) {
                    Text(
                        text = errorMessage.value ?: "Error desconocido",
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título de "Mis Recetas"
                Text(
                    text = "Mis Recetas",
                    color = PrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Lista de recetas del usuario
                if (userRecipes.value.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(userRecipes.value) { recipe ->
                            RecipeCard(recipe = recipe, onClick = { recipe.id?.let { onRecipeClick(it) } })
                        }
                    }
                } else {
                    Text(
                        text = "No tienes recetas aún.",
                        color = PrimaryDark.copy(alpha = 0.6f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}
