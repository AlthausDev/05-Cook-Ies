package com.althaus.dev.cookIes.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.data.model.Notification
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.PrimaryLight
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

//@Preview(showBackground = true)
//@Composable
//fun PreviewHomeView() {
//    HomeView(
//        recipeViewModel = RecipeViewModel(),
//        navigateToProfile = {},
//        onRecipeClick = {}
//    )
//}


@Composable
fun HomeView(
    recipeViewModel: RecipeViewModel,
    navigateToProfile: () -> Unit,
    navigateToNotifications: () -> Unit,
    navigateToFavorites: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val recipesState = remember { recipeViewModel.uiState }.collectAsState()
    val uiState = recipesState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryLight, PrimaryDark))),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezado
        HeaderSection(navigateToProfile)

        // Indicador de carga y mensajes de error
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = PrimaryDark, modifier = Modifier.padding(16.dp))
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                // Lista de recetas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.recipes.size) { index ->
                        RecipeCard(recipe = uiState.recipes[index], onRecipeClick = onRecipeClick)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHeaderSection() {
    HeaderSection(navigateToProfile = {})
}

@Composable
fun HeaderSection(navigateToProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recetario",
            color = PrimaryDark,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = navigateToProfile) {
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Perfil",
                tint = PrimaryDark
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRecipeCard() {
    RecipeCard(
        recipe = Recipe(
            id = "1",
            name = "Receta de prueba",
            description = "Deliciosa receta para probar la interfaz",
            difficultyLevel = 3
        ),
        onRecipeClick = {}
    )
}

@Composable
fun RecipeCard(recipe: Recipe, onRecipeClick: (Recipe) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { onRecipeClick(recipe) }
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            ImageSection(recipeImageRes = R.drawable.logo) // Imagen temporal
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = recipe.name ?: "Receta desconocida",
                    color = PrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recipe.description ?: "Descripción no disponible",
                    color = PrimaryDark.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Text(
                    text = "Dificultad: ${recipe.difficultyLevel}",
                    color = PrimaryDark,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewImageSection() {
    ImageSection(recipeImageRes = R.drawable.logo)
}

@Composable
fun ImageSection(recipeImageRes: Int) {
    Image(
        painter = painterResource(id = recipeImageRes),
        contentDescription = null,
        modifier = Modifier
            .size(80.dp)
            .background(Color.LightGray, CircleShape)
    )
}
