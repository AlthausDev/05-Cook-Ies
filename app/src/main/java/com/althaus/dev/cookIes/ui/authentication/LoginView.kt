package com.althaus.dev.cookIes.ui.authentication

import androidx.compose.foundation.clickable
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
 * Pantalla de inicio de sesión.
 *
 * Proporciona una interfaz para que los usuarios ingresen su correo electrónico y contraseña
 * para iniciar sesión. Incluye opciones para registrarse o restablecer la contraseña en caso de olvidarla.
 *
 * @param navigateToSignUp Callback que se ejecuta al hacer clic en el texto para registrarse.
 * @param onLoginSuccess Callback que se ejecuta cuando el inicio de sesión es exitoso.
 * @param navigateToForgotPassword Callback que se ejecuta al hacer clic en el texto para restablecer la contraseña.
 * @param authViewModel [AuthViewModel] que gestiona la lógica de autenticación.
 */
@Composable
fun LoginView(
    navigateToSignUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    navigateToForgotPassword: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    // ---- Estados locales ----
    var email by remember { mutableStateOf("") } // Correo electrónico ingresado por el usuario.
    var password by remember { mutableStateOf("") } // Contraseña ingresada por el usuario.

    // ---- Estados observados desde el ViewModel ----
    val user by authViewModel.user.collectAsState() // Usuario autenticado actual.
    val isLoading by authViewModel.isLoading.collectAsState() // Estado de carga durante la autenticación.
    val errorMessage by authViewModel.errorMessage.collectAsState() // Mensaje de error si ocurre algún fallo.

    // Efecto que detecta cambios en el usuario autenticado
    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
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
                text = "Iniciar Sesión",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.4f))

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

            Spacer(modifier = Modifier.weight(0.3f))

            // Botón para iniciar sesión
            PrimaryButton(
                text = "Iniciar Sesión",
                onClick = { authViewModel.login(email, password) }
            )

            // Indicador de carga mientras se procesa la autenticación
            if (isLoading) {
                SharedLoadingIndicator()
            }

            // Mostrar mensaje de error si ocurre un fallo
            errorMessage?.let {
                SharedErrorMessage(message = it)
            }

            // Texto clicable para registrarse
            ClickableText(
                text = "¿No tienes cuenta? Regístrate",
                onClick = navigateToSignUp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Texto clicable para restablecer la contraseña
            Text(
                text = "¿Olvidaste tu contraseña? Restablécela",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { navigateToForgotPassword() }
            )

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}
