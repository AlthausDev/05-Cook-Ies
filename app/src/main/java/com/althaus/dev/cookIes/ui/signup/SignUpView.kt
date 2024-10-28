package com.althaus.dev.cookIes.ui.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.ui.components.CustomTextField
import com.althaus.dev.cookIes.ui.startup.CustomButton
import com.althaus.dev.cookIes.ui.startup.ParchmentLight
import com.althaus.dev.cookIes.ui.startup.ParchmentDark
import com.althaus.dev.cookIes.ui.startup.TextBrown

@Preview
@Composable
fun SignUpView(navigateToLogin: () -> Unit = {}, onSignUp: () -> Unit = {}) {
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
        CustomTextField(placeholder = "Nombre Completo")

        // Campo de correo electrónico
        CustomTextField(placeholder = "Correo Electrónico")

        // Campo de contraseña
        CustomTextField(placeholder = "Contraseña", isPassword = true)

        // Campo de contraseña
        CustomTextField(placeholder = "Repetir Contraseña", isPassword = true)

        Spacer(modifier = Modifier.weight(0.25f))

        // Botón de registro
        Button(
            onClick = onSignUp,
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

        // Botón de Google
        CustomButton(
            modifier = Modifier.fillMaxWidth(0.8f),
            painter = painterResource(id = R.drawable.google),
            title = "Registrarse con Google"
        )

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
