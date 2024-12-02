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

/**
 * Pantalla de registro de usuario.
 *
 * Proporciona una interfaz para que los usuarios creen una nueva cuenta mediante la entrada de su nombre,
 * correo electrónico, contraseña y confirmación de contraseña. Incluye validación de contraseñas y muestra
 * mensajes de error en caso de fallos en el registro.
 *
 * @param navigateToLogin Callback que se ejecuta al hacer clic en el texto para ir a la pantalla de inicio de sesión.
 * @param onSignUpSuccess Callback que se ejecuta cuando el registro es exitoso.
 * @param authViewModel [AuthViewModel] que gestiona la lógica de registro y autenticación.
 */
@Composable
fun SignUpView(
    navigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    // ---- Campos de entrada ----
    var fullName by remember { mutableStateOf("") } // Nombre completo del usuario.
    var email by remember { mutableStateOf("") } // Correo electrónico ingresado por el usuario.
    var password by remember { mutableStateOf("") } // Contraseña ingresada por el usuario.
    var confirmPassword by remember { mutableStateOf("") } // Confirmación de contraseña.

    // ---- Estados observados desde el ViewModel ----
    val user by authViewModel.user.collectAsState() // Usuario autenticado actual.
    val isLoading by authViewModel.isLoading.collectAsState() // Estado de carga durante el registro.
    val errorMessage by authViewModel.errorMessage.collectAsState() // Mensaje de error si ocurre algún fallo.

    // Efecto que detecta cambios en el usuario autenticado
    LaunchedEffect(user) {
        if (user != null) onSignUpSuccess()
    }

    // Fondo degradado de la pantalla
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

            // Campo para ingresar el nombre completo
            CustomTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Nombre Completo"
            )

            // Campo para ingresar el correo electrónico
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo Electrónico"
            )

            // Campo para ingresar la contraseña
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                isPassword = true
            )

            // Campo para confirmar la contraseña
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
                    // Validar que las contraseñas coincidan antes de intentar registrar al usuario
                    if (authViewModel.validatePasswords(password, confirmPassword)) {
                        authViewModel.register(email, password, fullName)
                    }
                }
            )

            // Indicador de carga mientras se procesa el registro
            if (isLoading) {
                SharedLoadingIndicator()
            }

            // Mostrar mensaje de error si ocurre un fallo
            errorMessage?.let {
                SharedErrorMessage(message = it)
            }

            // Texto clicable para redirigir a la pantalla de inicio de sesión
            ClickableText(
                text = "¿Ya tienes cuenta? Inicia Sesión",
                onClick = navigateToLogin
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
