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
import com.althaus.dev.cookIes.theme.PrimaryButton
import com.althaus.dev.cookIes.theme.PrimaryDark
import com.althaus.dev.cookIes.ui.components.*
import com.althaus.dev.cookIes.viewmodel.AuthViewModel

@Composable
fun LoginView(
    navigateToSignUp: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    navigateToForgotPassword: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val user by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

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

            Text(
                text = "Iniciar Sesión",
                color = PrimaryDark,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.4f))

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

            Spacer(modifier = Modifier.weight(0.3f))

            PrimaryButton(
                text = "Iniciar Sesión",
                onClick = { authViewModel.login(email, password) }
            )

            if (isLoading) {
                LoadingIndicator()
            }

            errorMessage?.let {
                ErrorText(message = it)
            }

            ClickableText(
                text = "¿No tienes cuenta? Regístrate",
                onClick = navigateToSignUp
            )

            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = "¿Olvidaste tu contraseña? Restablécela aquí.",
                fontSize = 15.sp,
                color = PrimaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {navigateToForgotPassword() }
            )

            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}



