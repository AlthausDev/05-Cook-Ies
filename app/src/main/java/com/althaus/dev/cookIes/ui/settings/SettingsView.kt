package com.althaus.dev.cookIes.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.ui.components.PrimaryButton
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.SharedTopAppBar
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel

/**
 * Vista principal de configuración del usuario.
 *
 * Permite al usuario actualizar información de perfil como nombre, correo, contraseña,
 * y foto de perfil. También incluye opciones para cerrar sesión y alternar el tema de la aplicación.
 *
 * @param profileViewModel ViewModel para manejar la lógica de la configuración.
 * @param onCancel Callback que se ejecuta al regresar a la pantalla anterior.
 * @param onLogout Callback que se ejecuta al cerrar sesión.
 * @param onToggleTheme Callback para alternar entre tema claro y oscuro.
 */

@Composable
fun SettingsView(
    profileViewModel: ProfileViewModel,
    onCancel: () -> Unit,
    onLogout: () -> Unit,
    //isDarkTheme: Boolean,
    onToggleTheme: () -> Unit // Acción para alternar el tema
) {
    LaunchedEffect(Unit) {
        profileViewModel.clearError()
    }

    var isImageDialogOpen by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var isNameDialogOpen by remember { mutableStateOf(false) }
    var isEmailDialogOpen by remember { mutableStateOf(false) }
    var isPasswordDialogOpen by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(profileViewModel.userProfile.value?.name ?: "") }
    var email by remember { mutableStateOf(profileViewModel.userProfile.value?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val userProfile by profileViewModel.userProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            SharedTopAppBar(
                title = "Configuración",
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
        },
        actions = {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription =  "Modo claro",
                    tint = MaterialTheme.colorScheme.primary
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
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } ?: userProfile?.profileImage?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Foto de Perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } ?: Image(
                        painter = painterResource(id = R.drawable.default_profile),
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.weight(0.2f))

                // Botón para cambiar foto
                PrimaryButton(
                    text = "Cambiar Foto",
                    onClick = { isImageDialogOpen = true },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                // Botones de configuración
                PrimaryButton(text = "Cambiar Nombre", onClick = { isNameDialogOpen = true })
                PrimaryButton(text = "Cambiar Correo", onClick = { isEmailDialogOpen = true })
                PrimaryButton(text = "Cambiar Contraseña", onClick = { isPasswordDialogOpen = true })

                Spacer(modifier = Modifier.weight(0.2f))

                // Botón de cierre de sesión
                PrimaryButton(
                    text = "Cerrar Sesión",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    backgroundColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                // Indicador de carga
                if (isLoading) {
                    SharedLoadingIndicator()
                }

                // Mensaje de error
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )

    // Diálogos
    if (isNameDialogOpen) {
        EditNameDialog(
            currentName = userProfile?.name.orEmpty(),
            onSave = { newName ->
                profileViewModel.updateUserName(newName)
                isNameDialogOpen = false
            },
            onCancel = { isNameDialogOpen = false }
        )
    }

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

    if (isPasswordDialogOpen) {
        EditPasswordDialog(
            onSave = { currentPassword, newPassword ->
                profileViewModel.updateUserPassword(newPassword, currentPassword)
                isPasswordDialogOpen = false
            },
            onCancel = { isPasswordDialogOpen = false }
        )
    }

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
                Button(onClick = {
                    selectedImageUri?.let { uri ->
                        profileViewModel.updateProfileImage(Uri.parse(uri))
                        isImageDialogOpen = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { isImageDialogOpen = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Diálogo para editar información del usuario, como el correo electrónico.
 *
 * Presenta dos campos de entrada: uno para el nuevo valor y otro para la contraseña actual,
 * asegurando la autenticación antes de realizar cambios sensibles.
 *
 * @param title Título del diálogo.
 * @param value1 Valor actual o nuevo a actualizar.
 * @param onValue1Change Callback para actualizar el valor del primer campo.
 * @param value2 Contraseña actual para autenticar cambios.
 * @param onValue2Change Callback para actualizar el valor del segundo campo.
 * @param onSave Callback que se ejecuta al guardar los cambios.
 * @param onCancel Callback que se ejecuta al cerrar el diálogo sin guardar.
 */

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

/**
 * Diálogo para editar el nombre del usuario.
 *
 * Permite al usuario ingresar un nuevo nombre y guardarlo.
 *
 * @param currentName Nombre actual del usuario.
 * @param onSave Callback que se ejecuta al guardar el nuevo nombre.
 * @param onCancel Callback que se ejecuta al cerrar el diálogo sin guardar.
 */

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

/**
 * Diálogo para cambiar la contraseña del usuario.
 *
 * Permite al usuario ingresar su contraseña actual y una nueva contraseña,
 * validando ambos campos antes de guardar los cambios.
 *
 * @param onSave Callback que se ejecuta al guardar la nueva contraseña.
 * @param onCancel Callback que se ejecuta al cerrar el diálogo sin guardar.
 */

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
