package com.althaus.dev.cookIes.ui.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.althaus.dev.cookIes.data.model.Ingredient
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.navigation.Screen
import com.althaus.dev.cookIes.theme.GradientBackground
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun RecipeWizardView(
    firestoreRepository: FirestoreRepository,
    navHostController: NavHostController,
    currentAuthorId: String,
    onComplete: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    // Estado del paso actual
    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf("Información General", "Ingredientes", "Instrucciones")

    // Estados para almacenar los datos de la receta
    var recipeName by remember { mutableStateOf("") }
    var recipeDescription by remember { mutableStateOf("") }
    var recipeIngredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    var recipeInstructions by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }


        GradientBackground { // Fondo con gradiente
            Scaffold(
                topBar = {
                    SharedTopAppBar(
                        title = "Creación de Receta - Paso ${currentStep + 1}/${steps.size}"
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
                        // Renderizar el contenido según el paso actual
                        when (currentStep) {
                            0 -> GeneralInfoStep(
                                recipeName = recipeName,
                                onNameChange = { recipeName = it },
                                recipeDescription = recipeDescription,
                                onDescriptionChange = { recipeDescription = it }
                            )
                            1 -> IngredientsStep(
                                ingredients = recipeIngredients,
                                onAddIngredient = { ingredient ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val newId = firestoreRepository.generateNewId("ingredients")
                                            val newIngredient = ingredient.copy(id = newId)
                                            recipeIngredients = recipeIngredients + newIngredient
                                        } catch (e: Exception) {
                                            errorMessage = "Error al guardar el ingrediente: ${e.localizedMessage}"
                                        }
                                    }
                                },
                                onRemoveIngredient = { recipeIngredients = recipeIngredients - it },
                                firestoreRepository = firestoreRepository
                            )
                            2 -> InstructionsStep(
                                instructions = recipeInstructions,
                                onInstructionsChange = { recipeInstructions = it },
                                onSave = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val recipe = Recipe(
                                                name = recipeName,
                                                description = recipeDescription,
                                                ingredients = recipeIngredients,
                                                instructions = recipeInstructions
                                            )
                                            recipe.saveToFirestore(firestoreRepository, currentAuthorId)
                                            onComplete(recipe)
                                        } catch (e: Exception) {
                                            errorMessage = "Error al guardar la receta: ${e.localizedMessage}"
                                        }
                                    }
                                },
                                onNavigateToDashboard = {
                                    navHostController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Mensaje de error
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        // Navegación del wizard
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
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val recipe = Recipe(
                                            name = recipeName,
                                            description = recipeDescription,
                                            ingredients = recipeIngredients,
                                            instructions = recipeInstructions
                                        )
                                        recipe.saveToFirestore(firestoreRepository, currentAuthorId)
                                        onComplete(recipe)
                                        navHostController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = true }
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
    }


// ==================== VALIDACIÓN DEL PASO ====================
fun validateStep(step: Int, name: String, ingredients: List<Ingredient>, instructions: String): Boolean {
    return when (step) {
        0 -> name.isNotBlank() // El nombre no puede estar vacío
        1 -> ingredients.isNotEmpty() // Debe haber al menos un ingrediente
        2 -> instructions.isNotBlank() // Las instrucciones no pueden estar vacías
        else -> true
    }
}

// ==================== COMPONENTE SHAREDBUTTON ====================
@Composable
fun SharedButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    Button(
        onClick = onClick,
        colors = colors,
        modifier = modifier
    ) {
        Text(text = text)
    }
}

// ==================== COMPONENTES DEL WIZARD ====================

// Paso 1: Información General
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
            label = {
                Text(
                    text = "Nombre de la Receta",
                    color = MaterialTheme.colorScheme.primary // Color Primary para el label
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary // Color Primary para el texto de entrada
            )
        )
        TextField(
            value = recipeDescription,
            onValueChange = onDescriptionChange,
            label = {
                Text(
                    text = "Descripción de la Receta",
                    color = MaterialTheme.colorScheme.primary // Color Primary para el label
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary // Color Primary para el texto de entrada
            )
        )
    }
}

// Paso 2: Ingredientes
@Composable
fun IngredientsStep(
    ingredients: List<Ingredient>,
    onAddIngredient: (Ingredient) -> Unit,
    onRemoveIngredient: (Ingredient) -> Unit,
    firestoreRepository: FirestoreRepository
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Ingredientes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary // Encabezado en Primary
        )
        for (ingredient in ingredients) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ingredient.name} - ${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary // Texto de ingredientes en Primary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveIngredient(ingredient) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar ingrediente",
                        tint = MaterialTheme.colorScheme.error // Icono en color de error
                    )
                }
            }
        }
        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Agregar Ingrediente")
        }

        if (showDialog) {
            AddIngredientDialog(
                onAdd = { ingredient ->
                    onAddIngredient(ingredient)
                    showDialog = false
                },
                onCancel = { showDialog = false },
                firestoreRepository = firestoreRepository
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onAdd: (Ingredient) -> Unit,
    onCancel: () -> Unit,
    firestoreRepository: FirestoreRepository
) {
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQuantity by remember { mutableStateOf("1") } // Cantidad predeterminada
    var ingredientUnit by remember { mutableStateOf("Unidad/es") } // Unidad predeterminada
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    val unitOptions = listOf(
        "Unidad/es", "gr", "Kg", "ml", "L", "Taza", "Cucharada", "Cucharadita", "Pizca"
    )

    // Cargar sugerencias de ingredientes desde Firestore
    LaunchedEffect(ingredientName) {
        if (ingredientName.isNotBlank()) {
            suggestions = firestoreRepository.getIngredientNames()
                .filter { it.contains(ingredientName, ignoreCase = true) }
        } else {
            suggestions = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Agregar Ingrediente",
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Campo para el nombre del ingrediente con autocompletado
                Box {
                    TextField(
                        value = ingredientName,
                        onValueChange = { ingredientName = it },
                        label = {
                            Text(
                                text = "Nombre del Ingrediente",
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Mostrar sugerencias dinámicas
                    if (suggestions.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            items(suggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            ingredientName =
                                                suggestion// Autocompletar el campo
                                            suggestions = emptyList() // Ocultar sugerencias
                                        }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // Campo para la cantidad
                TextField(
                    value = ingredientQuantity,
                    onValueChange = { ingredientQuantity = it },
                    label = {
                        Text(
                            text = "Cantidad",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Menú desplegable para seleccionar la unidad
                Box {
                    Button(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ingredientUnit,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        unitOptions.forEach { unit ->
                            DropdownMenuItem(
                                onClick = {
                                    ingredientUnit = unit
                                    isDropdownExpanded = false
                                },
                                text = {
                                    Text(
                                        text = unit,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                // Mostrar mensajes de error si existen
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ingredientName.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                        return@Button
                    }
                    if (ingredientQuantity.toDoubleOrNull() == null) {
                        errorMessage = "La cantidad debe ser un número válido."
                        return@Button
                    }

                    // Crear y guardar el ingrediente
                    val ingredient = Ingredient(
                        id = firestoreRepository.generateNewId("ingredients"),
                        name = ingredientName,
                        quantity = ingredientQuantity.toDouble(),
                        unit = ingredientUnit
                    )
                    onAdd(ingredient)
                }
            ) {
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



// Paso 3: Instrucciones
@Composable
fun InstructionsStep(
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = instructions,
            onValueChange = onInstructionsChange,
            label = {
                Text(
                    text = "Instrucciones",
                    color = MaterialTheme.colorScheme.primary // Label en Primary
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary // Texto de entrada en Primary
            )
        )

        Button(
            onClick = {
                onSave()
                onNavigateToDashboard()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Guardar y Volver al Dashboard")
        }
    }
}


// Navegación del Wizard
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
        SharedButton(
            onClick = onCancel,
            text = "Cancelar",
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        )
        if (currentStep > 0) {
            SharedButton(onClick = onBack, text = "Atrás")
        }
        if (currentStep < stepsCount - 1) {
            SharedButton(onClick = onNext, text = "Siguiente")
        } else {
            SharedButton(onClick = onComplete, text = "Completar")
        }
    }
}