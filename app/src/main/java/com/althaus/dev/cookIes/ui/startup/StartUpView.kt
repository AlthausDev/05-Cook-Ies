package com.althaus.dev.cookIes.ui.startup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.ParchmentDark
import com.althaus.dev.cookIes.theme.ParchmentLight
import com.althaus.dev.cookIes.theme.TextBrown
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

    // Launcher para Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = AuthResultContract(authViewModel.getGoogleSignInClient())
    ) { idToken ->
        // Envía el idToken a AuthViewModel para autenticar en Firebase
        authViewModel.handleGoogleSignInResult(idToken)
    }


    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    LaunchedEffect(Unit) {
        authViewModel.resetError()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        Text(
            text = "Inspírate y Cocina",
            color = TextBrown,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Descubre y Comparte Recetas",
            color = TextBrown,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = navigateToLogin,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextBrown),
            shape = CircleShape
        ) {
            Text(
                text = "Iniciar Sesión",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        CustomButton(
            modifier = Modifier.fillMaxWidth(0.8f),
            painter = painterResource(id = R.drawable.google),
            title = "Iniciar con Google",
            onClick = { authViewModel.launchGoogleSignIn(googleSignInLauncher) }
        )

        // Cambia color a marrón oscuro
        Text(
            text = "¿No tienes cuenta? Regístrate",
            color = TextBrown,
            modifier = Modifier.clickable { navigateToSignUp() },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        if (isLoading) {
            CircularProgressIndicator(color = TextBrown)
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

// Botón personalizado con ícono y texto
@Composable
fun CustomButton(
    modifier: Modifier,
    painter: Painter,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, TextBrown, CircleShape)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 8.dp)
        )
        Text(
            text = title,
            color = TextBrown,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
