//package com.althaus.dev.cookIes.ui.recipe
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.althaus.dev.cookIes.data.model.Recipe
//
//@Composable
//fun RecipeDetailView(recipe: Recipe) {
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text(text = recipe.name, style = MaterialTheme.typography.h5)
//
//        recipe.imageUrl?.let { url ->
//            Image(
//                painter = rememberImagePainter(url),
//                contentDescription = "Imagen de ${recipe.name}",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .clip(RoundedCornerShape(8.dp))
//            )
//        }
//
//        Text(text = "Descripción: ${recipe.description}", style = MaterialTheme.typography.body1)
//        Text(text = "Tiempo de preparación: ${recipe.prepTimeMinutes} minutos", style = MaterialTheme.typography.body2)
//        Text(text = "Tiempo de cocción: ${recipe.cookTimeMinutes} minutos", style = MaterialTheme.typography.body2)
//        Text(text = "Calorías: ${recipe.totalCalories} kcal", style = MaterialTheme.typography.body2)
//        Text(text = "Porciones: ${recipe.servings}", style = MaterialTheme.typography.body2)
//        Text(text = "Nivel de dificultad: ${recipe.difficultyLevel}/5", style = MaterialTheme.typography.body2)
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(text = "Ingredientes:", style = MaterialTheme.typography.subtitle1)
//        recipe.ingredients.forEach { ingredient ->
//            Text(text = "- ${ingredient.name}", style = MaterialTheme.typography.body2)
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(text = "Instrucciones:", style = MaterialTheme.typography.subtitle1)
//        Text(text = recipe.instructions, style = MaterialTheme.typography.body2)
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(text = "Etiquetas: ${recipe.tags.joinToString(", ")}", style = MaterialTheme.typography.caption)
//    }
//}
