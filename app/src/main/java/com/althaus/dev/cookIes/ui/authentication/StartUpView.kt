package com.althaus.dev.cookIes.ui.authentication

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
import com.althaus.dev.cookIes.viewmodel.AuthResultContract
import com.althaus.dev.cookIes.viewmodel.AuthViewModel

@Composable
fun StartUpView(
    navigateToLogin: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val user by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = AuthResultContract(authViewModel.getGoogleSignInClient())
    ) { idToken ->
        authViewModel.handleGoogleSignInResult(idToken)
    }

    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    LaunchedEffect(Unit) {
        authViewModel.resetError()
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

            TitleAndSubtitle(
                title = "Inspírate y Cocina",
                subtitle = "Descubre y Comparte Recetas"
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Iniciar Sesión",
                onClick = navigateToLogin
            )

            CustomButton(
                modifier = Modifier.fillMaxWidth(0.8f),
                painter = painterResource(id = R.drawable.google),
                title = "Iniciar con Google",
                onClick = { authViewModel.launchGoogleSignIn(googleSignInLauncher) }
            )

            ClickableText(
                text = "¿No tienes cuenta? Regístrate",
                onClick = navigateToSignUp
            )

            if (isLoading) {
                LoadingIndicator()
            }

            errorMessage?.let {
                ErrorText(message = it)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
