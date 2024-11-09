package com.althaus.dev.cookIes.ui.startup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow

// Colores para el esquema papiro
val ParchmentLight = Color(0xFFF5F0DC)
val ParchmentDark = Color(0xFFD7C6A0)
val TextBrown = Color(0xFF6D4C41)

@Preview
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

    // Si el usuario está autenticado, navega al HomeView
    LaunchedEffect(user) {
        if (user != null) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Logo de la app
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
        )

        // Texto principal
        Text(
            text = "Inspírate y Cocina",
            color = TextBrown,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Subtítulo descriptivo
        Text(
            text = "Descubre y Comparte Recetas",
            color = TextBrown,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botón de inicio de sesión
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

        // Botón de Google
        CustomButton(
            modifier = Modifier.fillMaxWidth(0.8f),
            painter = painterResource(id = R.drawable.google),
            title = "Iniciar con Google",
            onClick = { authViewModel.loginWithGoogle("ID_TOKEN_PLACEHOLDER") } // Aquí se debe proveer el token real
        )

        Text(
            text = "¿No tienes cuenta? Regístrate",
            color = TextBrown,
            modifier = Modifier.clickable { navigateToSignUp() },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        // Indicador de carga
        if (isLoading) {
            CircularProgressIndicator(color = TextBrown)
        }

        // Mensaje de error
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
