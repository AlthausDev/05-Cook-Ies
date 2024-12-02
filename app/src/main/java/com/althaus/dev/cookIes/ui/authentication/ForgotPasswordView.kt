package com.althaus.dev.cookIes.ui.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.viewmodel.AuthViewModel

/**
 * Pantalla para el restablecimiento de contraseña.
 *
 * Esta pantalla permite al usuario introducir su correo electrónico para enviar un enlace de
 * recuperación de contraseña.
 *
 * @param authViewModel [AuthViewModel] utilizado para gestionar la lógica de autenticación.
 * @param onBack Callback que se ejecuta al presionar el botón de regresar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordView(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    // ---- Estados locales ----
    var email by remember { mutableStateOf(TextFieldValue("")) } // Estado para el correo electrónico ingresado
    var isLoading by remember { mutableStateOf(false) } // Estado de carga mientras se envía el correo
    var successMessage by remember { mutableStateOf<String?>(null) } // Mensaje de éxito
    var errorMessage by remember { mutableStateOf<String?>(null) } // Mensaje de error

    // Estructura de la pantalla
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mensaje descriptivo
                Text(
                    text = "Introduce tu correo electrónico para restablecer tu contraseña.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo de texto para el correo electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Botón para enviar el correo de recuperación
                Button(
                    onClick = {
                        isLoading = true // Activar estado de carga
                        authViewModel.sendPasswordResetEmail(email.text) { success, error ->
                            isLoading = false // Desactivar estado de carga
                            if (success) {
                                successMessage = "Correo de recuperación enviado con éxito."
                                errorMessage = null
                            } else {
                                successMessage = null
                                errorMessage = error ?: "Ocurrió un error al enviar el correo."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && email.text.isNotBlank() // Habilitar solo si hay texto y no está cargando
                ) {
                    Text(if (isLoading) "Enviando..." else "Enviar Correo")
                }

                // Mostrar mensaje de éxito si existe
                if (!successMessage.isNullOrEmpty()) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Mostrar mensaje de error si existe
                if (!errorMessage.isNullOrEmpty()) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
