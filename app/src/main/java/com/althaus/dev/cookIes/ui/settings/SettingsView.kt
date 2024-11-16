package com.althaus.dev.cookIes.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.theme.PrimaryLight
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    profileViewModel: ProfileViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf(profileViewModel.userProfile.value?.name ?: "") }
    var email by remember { mutableStateOf(profileViewModel.userProfile.value?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isEmailDialogOpen by remember { mutableStateOf(false) }
    var isPasswordDialogOpen by remember { mutableStateOf(false) }
    var isNameDialogOpen by remember { mutableStateOf(false) }

    val isLoading = profileViewModel.isLoading.collectAsState()
    val errorMessage = profileViewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Brush.verticalGradient(listOf(PrimaryLight, PrimaryDark)))
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Imagen de perfil editable
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile),
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Button(
                    onClick = { isEmailDialogOpen = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)

                    ) {
                    Text("Cambiar Foto")
                }

                // Botones para editar información
                Button(
                    onClick = { isNameDialogOpen = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Cambiar Nombre")
                }

                Button(
                    onClick = { isEmailDialogOpen = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Cambiar Correo")
                }

                Button(
                    onClick = { isPasswordDialogOpen = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                ) {
                    Text("Cambiar Contraseña")
                }

                // Botón para cerrar sesión
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
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
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )

    // Modal para cambiar nombre
    if (isNameDialogOpen) {
        EditDialog(
            title = "Cambiar Nombre",
            value1 = name,
            onValue1Change = { name = it },
            value2 = currentPassword,
            onValue2Change = { currentPassword = it },
            onSave = {
                profileViewModel.updateName(
                    newName = (profileViewModel.userProfile.value?.copy(name = name) ?: return@EditDialog).toString()
                )
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
        EditDialog(
            title = "Cambiar Contraseña",
            value1 = newPassword,
            onValue1Change = { newPassword = it },
            value2 = currentPassword,
            onValue2Change = { currentPassword = it },
            onSave = {
                profileViewModel.updateUserPassword(newPassword = newPassword, currentPassword = currentPassword)
                isPasswordDialogOpen = false
            },
            onCancel = { isPasswordDialogOpen = false }
        )
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
                    label = { Text("Nuevo Valor") },
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
