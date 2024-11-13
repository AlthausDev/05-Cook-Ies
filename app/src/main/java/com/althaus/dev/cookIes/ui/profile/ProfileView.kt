package com.althaus.dev.cookIes.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.ui.home.RecipeCard

import com.althaus.dev.cookIes.ui.theme.*
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel

@Preview
@Composable
fun ProfileView(
    profileViewModel: ProfileViewModel,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    // Recogemos el estado del perfil y recetas desde el ViewModel
    val userProfile = profileViewModel.userProfile.collectAsState()
    val userRecipes = profileViewModel.userRecipes.collectAsState()
    val isLoading = profileViewModel.isLoading.collectAsState()
    val errorMessage = profileViewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Mostrar foto y nombre del usuario
        userProfile.value?.let { profile ->
            val profileImage = profile.profileImage?.let {
                painterResource(id = R.drawable.default_profile) // ID de recurso predeterminado
            }

            Image(
                painter = profileImage ?: painterResource(id = R.drawable.default_profile),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White, CircleShape)
                    .padding(2.dp)
            )
            Text(
                text = profile.name ?: "Nombre del Usuario",
                color = TextBrown,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = profile.email ?: "correo@ejemplo.com",
                color = TextBrown.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para editar perfil
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextBrown),
            shape = CircleShape
        ) {
            Text(
                text = "Editar Perfil",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Botón para cerrar sesión
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = CircleShape
        ) {
            Text(
                text = "Cerrar Sesión",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicador de carga y mensaje de error
        if (isLoading.value) {
            CircularProgressIndicator(color = TextBrown)
        } else if (errorMessage.value != null) {
            Text(
                text = errorMessage.value ?: "Error desconocido",
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Lista de recetas propias del usuario
        Text(
            text = "Mis Recetas",
            color = TextBrown,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(userRecipes.value) { recipe ->
                RecipeCard(recipe = recipe, onRecipeClick = { recipe.id?.let { onRecipeClick(it) } })
            }
        }
    }
}
