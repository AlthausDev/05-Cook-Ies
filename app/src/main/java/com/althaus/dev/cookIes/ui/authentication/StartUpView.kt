package com.althaus.dev.cookIes.ui.authentication

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.DarkOnPrimary
import com.althaus.dev.cookIes.theme.DarkSecondary
import com.althaus.dev.cookIes.theme.GradientBackground
import com.althaus.dev.cookIes.ui.components.AppLogo
import com.althaus.dev.cookIes.ui.components.ClickableText
import com.althaus.dev.cookIes.ui.components.PrimaryButton
import com.althaus.dev.cookIes.ui.components.SharedErrorMessage
import com.althaus.dev.cookIes.ui.components.SharedLoadingIndicator
import com.althaus.dev.cookIes.ui.components.TitleAndSubtitle
import com.althaus.dev.cookIes.viewmodel.AuthViewModel

/**
 * Pantalla inicial de la aplicación.
 *
 * Proporciona opciones para que el usuario inicie sesión, se registre o acceda con su cuenta de Google.
 * También muestra un mensaje de error en caso de fallos y un indicador de carga mientras se procesa una acción.
 *
 * @param navigateToLogin Callback que se ejecuta cuando el usuario hace clic en "Iniciar Sesión".
 * @param navigateToSignUp Callback que se ejecuta cuando el usuario hace clic en "¿No tienes cuenta? Regístrate".
 * @param authViewModel [AuthViewModel] que gestiona la lógica de autenticación.
 * @param onLoginSuccess Callback que se ejecuta cuando el usuario se autentica con éxito.
 */
@Composable
fun StartUpView(
    navigateToLogin: () -> Unit,
    navigateToSignUp: () -> Unit,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    // ---- Estados observados desde el ViewModel ----
    val user by authViewModel.user.collectAsState() // Usuario autenticado actual.
    val isLoading by authViewModel.isLoading.collectAsState() // Estado de carga durante la autenticación.
    val errorMessage by authViewModel.errorMessage.collectAsState() // Mensaje de error si ocurre algún fallo.

    // ---- Contexto y cliente de Google Sign-In ----
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    // Configuración del cliente de Google Sign-In
    val googleSignInClient = activity?.let { authViewModel.getGoogleSignInClient(it) }

    // Lanzador para el flujo de autenticación de Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = AuthResultContract(authViewModel.getGoogleSignInClient(activity))
    ) { idToken ->
        idToken?.let { authViewModel.handleGoogleSignInResult(it) }
    }

    // Redirigir al éxito si el usuario ya está autenticado
    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    // ---- Interfaz de Usuario ----
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logotipo de la aplicación
            AppLogo()

            // Título y subtítulo
            TitleAndSubtitle(
                title = "Inspírate y Cocina",
                subtitle = "Descubre y Comparte Recetas"
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón para iniciar sesión
            PrimaryButton(
                text = "Iniciar Sesión",
                onClick = navigateToLogin
            )

            // Botón para iniciar sesión con Google
            PrimaryButton(
                text = "Iniciar con Google",
                onClick = {
                    googleSignInLauncher?.let { launcher ->
                        authViewModel.launchGoogleSignIn(launcher)
                    }
                },
                icon = painterResource(id = R.drawable.google), // Icono de Google
                backgroundColor = Color.White, // Fondo dinámico
                contentColor = if (isSystemInDarkTheme()) DarkOnPrimary else MaterialTheme.colorScheme.primary, // Texto dinámico
                borderColor = if (isSystemInDarkTheme()) DarkSecondary else MaterialTheme.colorScheme.primary // Borde dinámico
            )

            // Texto clicable para redirigir a la pantalla de registro
            ClickableText(
                text = "¿No tienes cuenta? Regístrate",
                onClick = navigateToSignUp
            )

            // Indicador de carga mientras se procesa la acción
            if (isLoading) {
                SharedLoadingIndicator()
            }

            // Mostrar mensaje de error si ocurre un fallo
            errorMessage?.let { msg ->
                SharedErrorMessage(message = msg)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
