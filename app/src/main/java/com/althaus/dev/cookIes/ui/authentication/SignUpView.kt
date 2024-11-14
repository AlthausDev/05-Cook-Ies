package com.althaus.dev.cookIes.ui.authentication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.ui.components.CustomTextField
import com.althaus.dev.cookIes.theme.ParchmentDark
import com.althaus.dev.cookIes.theme.ParchmentLight
import com.althaus.dev.cookIes.theme.TextBrown
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Título de la pantalla
        Text(
            text = "Crear Cuenta",
            color = TextBrown,
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
        Button(
            onClick = {
                if (password == confirmPassword) {
                    authViewModel.register(email, password)
                } else {
                    authViewModel.setErrorMessage("Las contraseñas no coinciden")
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextBrown),
            shape = CircleShape
        ) {
            Text(
                text = "Registrarse",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

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


        // Redirección a la pantalla de inicio de sesión
        Text(
            text = "¿Ya tienes cuenta? Inicia Sesión",
            color = TextBrown,
            modifier = Modifier.clickable { navigateToLogin() },
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
