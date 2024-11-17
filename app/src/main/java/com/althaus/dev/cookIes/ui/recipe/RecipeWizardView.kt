package com.althaus.dev.cookIes.ui.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.althaus.dev.cookIes.data.model.Ingredient
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeWizardView(
    firestoreRepository: FirestoreRepository,
    navHostController: NavHostController,
    onComplete: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf("Información General", "Ingredientes", "Instrucciones")
    var recipeName by remember { mutableStateOf("") }
    var recipeDescription by remember { mutableStateOf("") }
    var recipeIngredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    var recipeInstructions by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Creación de Receta - Paso ${currentStep + 1}/${steps.size}") }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                when (currentStep) {
                    0 -> GeneralInfoStep(
                        recipeName = recipeName,
                        onNameChange = { recipeName = it },
                        recipeDescription = recipeDescription,
                        onDescriptionChange = { recipeDescription = it }
                    )
                    1 -> IngredientsStep(
                        ingredients = recipeIngredients,
                        onAddIngredient = { recipeIngredients = recipeIngredients + it },
                        onRemoveIngredient = { recipeIngredients = recipeIngredients - it }
                    )
                    2 -> InstructionsStep(
                        instructions = recipeInstructions,
                        onInstructionsChange = { recipeInstructions = it },
                        onSave = { // Guarda en Firestore
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val recipe = Recipe(
                                        name = recipeName,
                                        description = recipeDescription,
                                        ingredients = recipeIngredients,
                                        instructions = recipeInstructions
                                    )
                                    recipe.saveToFirestore(firestoreRepository)
                                    onComplete(recipe)
                                } catch (e: Exception) {
                                    errorMessage = "Error al guardar la receta: ${e.localizedMessage}"
                                }
                            }
                        },
                        onNavigateToDashboard = {
                            navHostController.navigate(Screen.Dashboard.route) {
                                popUpTo(0) { inclusive = true } // Limpia completamente el backstack
                            }
                        }
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                WizardNavigation(
                    currentStep = currentStep,
                    stepsCount = steps.size,
                    onNext = {
                        if (validateStep(currentStep, recipeName, recipeIngredients, recipeInstructions)) {
                            currentStep++
                            errorMessage = null
                        } else {
                            errorMessage = "Por favor, completa todos los campos requeridos antes de continuar."
                        }
                    },
                    onBack = { currentStep-- },
                    onComplete = {
                        val recipe = Recipe(
                            name = recipeName,
                            description = recipeDescription,
                            ingredients = recipeIngredients,
                            instructions = recipeInstructions
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                recipe.saveToFirestore(firestoreRepository)
                                onComplete(recipe)
                                navHostController.navigate(Screen.Dashboard.route) {
                                    popUpTo(0) { inclusive = true } // Limpia completamente el backstack
                                }

                            } catch (e: Exception) {
                                errorMessage = "Error al guardar la receta: ${e.localizedMessage}"
                            }
                        }
                    },
                    onCancel = onCancel
                )
            }
        }
    )
}



fun validateStep(step: Int, name: String, ingredients: List<Ingredient>, instructions: String): Boolean {
    return when (step) {
        0 -> name.isNotBlank()
        1 -> ingredients.isNotEmpty()
        2 -> instructions.isNotBlank()
        else -> true
    }
}

@Composable
fun GeneralInfoStep(
    recipeName: String,
    onNameChange: (String) -> Unit,
    recipeDescription: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextField(
            value = recipeName,
            onValueChange = onNameChange,
            label = { Text("Nombre de la Receta") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = recipeDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción de la Receta") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun IngredientsStep(
    ingredients: List<Ingredient>,
    onAddIngredient: (Ingredient) -> Unit,
    onRemoveIngredient: (Ingredient) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ingredientes", style = MaterialTheme.typography.titleLarge)
        for (ingredient in ingredients) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${ingredient.name} - ${ingredient.quantity} ${ingredient.unit}")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveIngredient(ingredient) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar ingrediente")
                }
            }
        }
        Button(onClick = { showDialog = true }) {
            Text("Agregar Ingrediente")
        }

        if (showDialog) {
            AddIngredientDialog(
                onAdd = {
                    onAddIngredient(it)
                    showDialog = false
                },
                onCancel = { showDialog = false }
            )
        }
    }
}

@Composable
fun AddIngredientDialog(
    onAdd: (Ingredient) -> Unit,
    onCancel: () -> Unit
) {
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQuantity by remember { mutableStateOf("1.0") }
    var ingredientUnit by remember { mutableStateOf("unidad") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Agregar Ingrediente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Nombre del Ingrediente") }
                )
                TextField(
                    value = ingredientQuantity,
                    onValueChange = { ingredientQuantity = it },
                    label = { Text("Cantidad") }
                )
                TextField(
                    value = ingredientUnit,
                    onValueChange = { ingredientUnit = it },
                    label = { Text("Unidad") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(Ingredient(name = ingredientName, quantity = ingredientQuantity.toDouble(), unit = ingredientUnit))
            }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun InstructionsStep(
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateToDashboard: () -> Unit // Nuevo callback para navegación explícita
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = instructions,
            onValueChange = onInstructionsChange,
            label = { Text("Instrucciones") },
            modifier = Modifier.fillMaxWidth()
        )

        // Botón para guardar y volver al Dashboard
        Button(
            onClick = {
                onSave()
                onNavigateToDashboard()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text("Guardar y Volver al Dashboard")
        }
    }
}

@Composable
fun WizardNavigation(
    currentStep: Int,
    stepsCount: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
        Text("Cancelar")
        }

        if (currentStep > 0) {
            Button(onClick = onBack) {
                Text("Atrás")
            }
        }
        if (currentStep < stepsCount - 1) {
            Button(onClick = onNext) {
                Text("Siguiente")
            }
        } else {
            Button(onClick = onComplete) {
                Text("Completar")
            }
        }

    }
}
