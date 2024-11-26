package com.althaus.dev.cookIes.ui.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.theme.GradientBackground
import com.althaus.dev.cookIes.ui.components.*
import com.althaus.dev.cookIes.viewmodel.AuthViewModel

@Composable
fun SignUpView(
    navigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    // Campos de entrada
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados desde el ViewModel
    val user by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // Navegar al HomeView si el usuario se registra correctamente
    LaunchedEffect(user) {
        if (user != null) onSignUpSuccess()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Título de la pantalla
            Text(
                text = "Crear Cuenta",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Campo de nombre
            CustomTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Nombre Completo"
            )

            // Campo de correo electrónico
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo Electrónico"
            )

            // Campo de contraseña
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                isPassword = true
            )

            // Campo de confirmación de contraseña
            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Repetir Contraseña",
                isPassword = true
            )

            Spacer(modifier = Modifier.weight(0.25f))

            // Botón de registro
            PrimaryButton(
                text = "Registrarse",
                onClick = {
                    if (authViewModel.validatePasswords(password, confirmPassword)) {
                        authViewModel.register(email, password, fullName)
                    }
                }
            )


            // Indicador de carga
            if (isLoading) {
                SharedLoadingIndicator()
            }

            // Mensaje de error
            errorMessage?.let {
                SharedErrorMessage(message = it)
            }

            // Redirección a la pantalla de inicio de sesión
            ClickableText(
                text = "¿Ya tienes cuenta? Inicia Sesión",
                onClick = navigateToLogin
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
