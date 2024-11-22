package com.althaus.dev.cookIes.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.ErrorLight
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.SecondaryLight
import com.althaus.dev.cookIes.theme.PrimaryLight
import com.althaus.dev.cookIes.ui.components.TopBarWithBack
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    profileViewModel: ProfileViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {

    // Restablecer el estado de error al salir de la vista
    LaunchedEffect(Unit) {
        profileViewModel.clearError()
    }


    // Estado local para manejar la URI de la foto seleccionada
    var isImageDialogOpen by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf(profileViewModel.userProfile.value?.name ?: "") }
    var email by remember { mutableStateOf(profileViewModel.userProfile.value?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isEmailDialogOpen by remember { mutableStateOf(false) }
    var isPasswordDialogOpen by remember { mutableStateOf(false) }
    var isNameDialogOpen by remember { mutableStateOf(false) }

    val userProfile = profileViewModel.userProfile.collectAsState()
    val isLoading = profileViewModel.isLoading.collectAsState()
    val errorMessage = profileViewModel.errorMessage.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            TopBarWithBack(
                title = "Configuración",
                onBack = onCancel
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Brush.verticalGradient(listOf(PrimaryLight, SecondaryLight)))
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Imagen de perfil
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    selectedImageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Vista previa",
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: userProfile.value?.profileImage?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Foto de Perfil",
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Image(
                        painter = painterResource(id = R.drawable.default_profile),
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para cambiar foto
                Button(
                    onClick = { isImageDialogOpen = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryLight),
                    shape = CircleShape
                ) {
                    Text("Cambiar Foto")
                }

                SettingsButton(
                    text = "Cambiar Nombre",
                    onClick = { isNameDialogOpen = true }
                )

                SettingsButton(
                    text = "Cambiar Correo",
                    onClick = { isEmailDialogOpen = true }
                )

                SettingsButton(
                    text = "Cambiar Contraseña",
                    onClick = { isPasswordDialogOpen = true }
                )

                Spacer(modifier = Modifier.weight(0.2f))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorLight),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Cerrar Sesión",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Indicador de carga
                if (isLoading.value) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                // Mensaje de error
                errorMessage.value?.let {
                    Text(
                        text = it,
                        color = ErrorLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )

    // Modal para cambiar el nombre del usuario
    if (isNameDialogOpen) {
        EditNameDialog(
            currentName = userProfile.value?.name ?: "",
            onSave = { newName ->
                profileViewModel.updateUserName(newName)
                isNameDialogOpen = false
            },
            onCancel = { isNameDialogOpen = false }
        )
    }

    // Modal para cambiar correo
    if (isEmailDialogOpen) {
        EditDialog(
            title = "Cambiar Correo",
            value1 = email,
            onValue1Change = { email = it },
            value2 = currentPassword,
            onValue2Change = { currentPassword = it },
            onSave = {
                profileViewModel.updateUserEmail(newEmail = email, currentPassword = currentPassword)
                isEmailDialogOpen = false
            },
            onCancel = { isEmailDialogOpen = false }
        )
    }


    // Modal para cambiar contraseña
    if (isPasswordDialogOpen) {
        EditPasswordDialog(
            onSave = { currentPassword, newPassword ->
                profileViewModel.updateUserPassword(newPassword, currentPassword)
                isPasswordDialogOpen = false
            },
            onCancel = { isPasswordDialogOpen = false }
        )
    }

    // Modal para cambiar foto
    if (isImageDialogOpen) {
        AlertDialog(
            onDismissRequest = { isImageDialogOpen = false },
            title = { Text("Cambiar Foto de Perfil") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    selectedImageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Vista previa",
                            modifier = Modifier.size(120.dp).clip(CircleShape)
                        )
                    } ?: Text("No se ha seleccionado ninguna imagen.")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Seleccionar Foto")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedImageUri?.let { uri ->
                            if (Uri.parse(uri).isAbsolute) {
                                profileViewModel.updateProfileImage(Uri.parse(uri))
                                isImageDialogOpen = false
                            } else {
                                profileViewModel.showError("URI de la imagen no válida.")
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        selectedImageUri = null
                        isImageDialogOpen = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )

        // Mensaje de error
        errorMessage.value?.let {
            Text(
                text = it,
                color = ErrorLight,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditDialog(
    title: String,
    value1: String,
    onValue1Change: (String) -> Unit,
    value2: String,
    onValue2Change: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = title)
        },
        text = {
            Column {
                TextField(
                    value = value1,
                    onValueChange = onValue1Change,
                    label = { Text("Nuevo Correo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = value2,
                    onValueChange = onValue2Change,
                    label = { Text("Contraseña Actual") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Guardar")
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
fun EditNameDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Cambiar Nombre") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nuevo Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim())
                    }
                }
            ) {
                Text("Guardar")
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
fun EditPasswordDialog(
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Cambiar Contraseña") },
        text = {
            Column {
                TextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña Actual") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva Contraseña") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                        onSave(currentPassword.trim(), newPassword.trim())
                    }
                }
            ) {
                Text("Guardar")
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
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = SecondaryLight,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .border(
                width = 3.dp,
                color = PrimaryDark.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = CircleShape
    ) {
        Text(text)
    }
}

