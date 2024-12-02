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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.althaus.dev.cookIes.data.model.Ingredient
import com.althaus.dev.cookIes.data.model.Recipe
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.navigation.Screen
import com.althaus.dev.cookIes.theme.GradientBackground
import com.althaus.dev.cookIes.ui.components.PrimaryButton
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Vista principal para el asistente de creación de recetas.
 *
 * Permite al usuario completar un proceso paso a paso para crear una nueva receta,
 * ingresando información general, ingredientes e instrucciones.
 *
 * @param firestoreRepository Repositorio de Firestore para guardar la receta.
 * @param navHostController Controlador de navegación para redirigir a otras vistas.
 * @param currentAuthorId ID del autor actual que crea la receta.
 * @param onComplete Callback que se ejecuta cuando se completa la receta.
 * @param onCancel Callback que se ejecuta al cancelar el asistente.
 */

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

    // Fondo con gradiente
    GradientBackground {
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
                ) {
                    // Contenido principal (Pasos del wizard)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
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

                        // Mensaje de error en el centro
                        errorMessage?.let {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center // Centrar el mensaje
                            ) {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center, // Asegura que el texto esté centrado
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }


                    // Botones de navegación (siempre visibles)
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
                        onBack = {
                            currentStep--
                            errorMessage = null
                        },
                        onSave = {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val recipe = Recipe(
                                        name = recipeName,
                                        description = recipeDescription,
                                        ingredients = recipeIngredients,
                                        instructions = recipeInstructions
                                    )
                                    recipe.saveToFirestore(firestoreRepository, currentAuthorId) // Guardar receta
                                    onComplete(recipe)
                                    navHostController.navigate(Screen.Dashboard.route) { // Navega al Dashboard
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error al guardar la receta: ${e.localizedMessage}"
                                }
                            }
                        },
                        onCancel = onCancel,
                        onNavigateToDashboard = {
                            navHostController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.weight(0.1f))
                }
            }
        )
    }
}

/**
 * Valida los campos requeridos en cada paso del asistente.
 *
 * Este método asegura que los datos ingresados en cada paso sean válidos antes de continuar.
 *
 * @param step El número del paso actual.
 * @param name El nombre de la receta.
 * @param ingredients Lista de ingredientes agregados.
 * @param instructions Las instrucciones de la receta.
 * @return `true` si los datos del paso son válidos, `false` de lo contrario.
 */

// ==================== VALIDACIÓN DEL PASO ====================
fun validateStep(step: Int, name: String, ingredients: List<Ingredient>, instructions: String): Boolean {
    return when (step) {
        0 -> name.isNotBlank() // El nombre no puede estar vacío
        1 -> ingredients.isNotEmpty() // Debe haber al menos un ingrediente
        2 -> instructions.isNotBlank() // Las instrucciones no pueden estar vacías
        else -> true
    }
}


// ==================== COMPONENTES DEL WIZARD ====================

/**
 * Paso 1: Ingreso de información general de la receta.
 *
 * Permite al usuario ingresar el nombre y una descripción breve de la receta.
 *
 * @param recipeName Nombre de la receta.
 * @param onNameChange Callback para actualizar el nombre de la receta.
 * @param recipeDescription Descripción de la receta.
 * @param onDescriptionChange Callback para actualizar la descripción de la receta.
 */
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

/**
 * Paso 2: Ingreso de ingredientes para la receta.
 *
 * Permite al usuario agregar, eliminar y gestionar ingredientes.
 *
 * @param ingredients Lista actual de ingredientes.
 * @param onAddIngredient Callback para agregar un ingrediente.
 * @param onRemoveIngredient Callback para eliminar un ingrediente.
 * @param firestoreRepository Repositorio de Firestore para manejar sugerencias y validaciones.
 */

@Composable
fun IngredientsStep(
    ingredients: List<Ingredient>,
    onAddIngredient: (Ingredient) -> Unit,
    onRemoveIngredient: (Ingredient) -> Unit,
    firestoreRepository: FirestoreRepository
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Margen general
        verticalArrangement = Arrangement.Top, // Contenido en la parte superior
        horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente
    ) {
        // Título
        Text(
            text = "Ingredientes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Lista de ingredientes
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .weight(1f) // Toma espacio disponible sin interferir con botones de navegación
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ingredients) { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ingredient.name} - ${ingredient.quantity} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveIngredient(ingredient) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar ingrediente",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Botón "Agregar Ingrediente"
        PrimaryButton(
            text = "Agregar Ingrediente",
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth(0.9f) // Ajusta el ancho al 90%
                .padding(top = 16.dp)
        )

        // Mostrar el diálogo para agregar ingredientes
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

/**
 * Diálogo para agregar un nuevo ingrediente.
 *
 * Permite al usuario ingresar el nombre, cantidad y unidad de un ingrediente,
 * con opciones de autocompletado basadas en datos de Firestore.
 *
 * @param onAdd Callback para agregar el ingrediente ingresado.
 * @param onCancel Callback para cerrar el diálogo sin guardar.
 * @param firestoreRepository Repositorio de Firestore para cargar sugerencias.
 */

@Composable
fun AddIngredientDialog(
    onAdd: (Ingredient) -> Unit,
    onCancel: () -> Unit,
    firestoreRepository: FirestoreRepository
) {
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQuantity by remember { mutableStateOf("1") }
    var ingredientUnit by remember { mutableStateOf("Unidad/es") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    val unitOptions = listOf(
        "Unidad/es", "gr", "Kg", "ml", "L", "Taza", "Cucharada", "Cucharadita", "Pizca"
    )

    // Cargar sugerencias de ingredientes desde Firestore
    LaunchedEffect(ingredientName) {
        if (ingredientName.isNotBlank()) {
            try {
                // Acceso a Firebase en un hilo seguro
                val fetchedSuggestions = withContext(Dispatchers.IO) {
                    firestoreRepository.getIngredientNames()
                        .filter { it.contains(ingredientName, ignoreCase = true) }
                }
                // Actualizar estado en el hilo principal
                withContext(Dispatchers.Main) {
                    suggestions = fetchedSuggestions
                }
            } catch (e: Exception) {
                // Manejar errores de Firebase
                withContext(Dispatchers.Main) {
                    suggestions = emptyList()
                    errorMessage = "Error al cargar sugerencias: ${e.localizedMessage}"
                }
            }
        } else {
            suggestions = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Agregar Ingrediente",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center // Centrar el título
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Centrar horizontalmente los elementos
                modifier = Modifier.fillMaxWidth()
            ) {
                // Campo para el nombre del ingrediente
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
                    modifier = Modifier.fillMaxWidth(0.9f) // Reducir ancho al 90%
                )

                // Mostrar sugerencias dinámicas
                if (suggestions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        items(suggestions) { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ingredientName = suggestion // Autocompletar el campo
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
                    ),
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                // Menú desplegable para seleccionar la unidad
                Box(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { isDropdownExpanded = true },
                        shape = MaterialTheme.shapes.medium, // Esquinas cuadradas
                        modifier = Modifier
                            .height(40.dp)
                            .width(150.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(text = ingredientUnit)
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

                // Mensaje de error si existe
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
            PrimaryButton(
                text = "Agregar",
                onClick = {
                    if (ingredientName.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                        return@PrimaryButton
                    }
                    if (ingredientQuantity.toDoubleOrNull() == null) {
                        errorMessage = "La cantidad debe ser un número válido."
                        return@PrimaryButton
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val ingredientId = firestoreRepository.generateNewId("ingredients")
                            val ingredient = Ingredient(
                                id = ingredientId,
                                name = ingredientName,
                                quantity = ingredientQuantity.toDouble(),
                                unit = ingredientUnit
                            )

                            // Guardar en Firebase
                            firestoreRepository.saveIngredient(
                                ingredientId,
                                mapOf(
                                    "id" to ingredient.id,
                                    "name" to ingredient.name,
                                    "quantity" to ingredient.quantity,
                                    "unit" to ingredient.unit
                                )
                            )

                            // Actualizar UI en el hilo principal
                            withContext(Dispatchers.Main) {
                                onAdd(ingredient) // Notificar al padre que se agregó el ingrediente
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                errorMessage = "Error al guardar el ingrediente: ${e.localizedMessage}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            )

        },
        dismissButton = {
            PrimaryButton(
                text = "Cancelar",
                onClick = onCancel,
                backgroundColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    )
}

/**
 * Paso 3: Ingreso de instrucciones para la receta.
 *
 * Permite al usuario escribir las instrucciones de preparación de la receta.
 *
 * @param instructions Las instrucciones actuales de la receta.
 * @param onInstructionsChange Callback para actualizar las instrucciones.
 * @param onSave Callback para guardar la receta.
 * @param onNavigateToDashboard Callback para redirigir al Dashboard tras guardar.
 */

@Composable
fun InstructionsStep(
    instructions: String,
    onInstructionsChange: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = instructions,
            onValueChange = onInstructionsChange,
            label = {
                Text(
                    text = "Instrucciones",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Controles de navegación para el asistente de creación de recetas.
 *
 * Incluye botones para continuar, regresar, guardar y cancelar.
 *
 * @param currentStep El paso actual en el asistente.
 * @param stepsCount El número total de pasos en el asistente.
 * @param onNext Callback para avanzar al siguiente paso.
 * @param onBack Callback para regresar al paso anterior.
 * @param onSave Callback para guardar la receta y finalizar el asistente.
 * @param onCancel Callback para cancelar el asistente.
 * @param onNavigateToDashboard Callback para navegar al Dashboard.
 */

@Composable
fun WizardNavigation(
    currentStep: Int,
    stepsCount: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToDashboard: () -> Unit // Agregar este parámetro
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Botón de "Cancelar" solo en el primer paso
        if (currentStep == 0) {
            PrimaryButton(
                text = "Cancelar",
                onClick = onCancel,
                backgroundColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }

        // Botón de "Atrás" en pasos posteriores al primero
        if (currentStep > 0) {
            PrimaryButton(
                text = "Atrás",
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }

        // Botón "Continuar" o "Guardar y Volver al Dashboard"
        if (currentStep < stepsCount - 1) {
            PrimaryButton(
                text = "Continuar",
                onClick = onNext,
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        } else {
            PrimaryButton(
                text = "Guardar y Volver al Dashboard",
                onClick = onSave,
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
    }
}

