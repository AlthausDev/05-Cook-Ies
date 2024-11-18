package com.althaus.dev.cookIes.ui.authentication

import AuthResultContract
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.GradientBackground
import com.althaus.dev.cookIes.theme.PrimaryButton
import com.althaus.dev.cookIes.ui.components.AppLogo
import com.althaus.dev.cookIes.ui.components.ClickableText
import com.althaus.dev.cookIes.ui.components.CustomButton
import com.althaus.dev.cookIes.ui.components.ErrorText
import com.althaus.dev.cookIes.ui.components.LoadingIndicator
import com.althaus.dev.cookIes.ui.components.TitleAndSubtitle
import com.althaus.dev.cookIes.viewmodel.AuthViewModel
import com.google.api.Context

@Composable
fun StartUpView(
    navigateToLogin: () -> Unit,
    navigateToSignUp: () -> Unit,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val user by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    // Configuración del cliente de Google Sign-In
    val googleSignInClient = activity?.let { authViewModel.getGoogleSignInClient(it) }

    // Comprobar si `googleSignInClient` no es nulo antes de crear el contrato
    val googleSignInLauncher = googleSignInClient?.let {
        rememberLauncherForActivityResult(
            contract = AuthResultContract(it)
        ) { idToken ->
            idToken?.let { authViewModel.handleGoogleSignInResult(it) }
        }
    }

    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    // Verificar si el usuario ya está autenticado y redirigir según corresponda
    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
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

            AppLogo()

            // Título y subtítulo
            TitleAndSubtitle(
                title = "Inspírate y Cocina",
                subtitle = "Descubre y Comparte Recetas"
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Iniciar Sesión",
                onClick = navigateToLogin
            )

            // Botón para iniciar sesión con Google
            CustomButton(
                modifier = Modifier.fillMaxWidth(0.8f),
                painter = painterResource(id = R.drawable.google),
                title = "Iniciar con Google",
                onClick = {
                    if (googleSignInLauncher != null) {
                        authViewModel.launchGoogleSignIn(googleSignInLauncher)
                    }
                }
            )

            // Enlace a registro
            ClickableText(
                text = "¿No tienes cuenta? Regístrate",
                onClick = navigateToSignUp
            )

            // Indicador de carga
            if (isLoading) {
                LoadingIndicator()
            }

            // Mensaje de error
            errorMessage?.let { msg ->
                ErrorText(message = msg)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
