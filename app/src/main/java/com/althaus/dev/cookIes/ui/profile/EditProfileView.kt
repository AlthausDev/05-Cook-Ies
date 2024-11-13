package com.althaus.dev.cookIes.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.althaus.dev.cookIes.ui.components.CustomTextField

import com.althaus.dev.cookIes.ui.theme.*

import com.althaus.dev.cookIes.viewmodel.ProfileViewModel

@Preview
@Composable
fun EditProfileView(
    profileViewModel: ProfileViewModel,
    onSaveChanges: () -> Unit,
    onCancel: () -> Unit
) {
    // Campos de entrada del usuario
    var name by remember { mutableStateOf(profileViewModel.userProfile.value?.name ?: "") }
    var email by remember { mutableStateOf(profileViewModel.userProfile.value?.email ?: "") }
    var selectedImage: Painter? = painterResource(id = R.drawable.default_profile)

    // Estados desde el ViewModel
    val isLoading = profileViewModel.isLoading.collectAsState()
    val errorMessage = profileViewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Foto de perfil con botón para cambiarla
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White, CircleShape)
                .clickable {
                    // Aquí se llamaría al selector de imagen
                    //selectedImage = painterResource(id = R.drawable.new_selected_image)
                }
        ) {
            selectedImage?.let {
                Image(
                    painter = it,
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier.size(120.dp)
                )
            }
        }
        Text(
            text = "Cambiar foto",
            color = TextBrown,
            fontSize = 14.sp,
            modifier = Modifier.clickable { /* Abrir selector de imagen */ }
        )

        // Campo de nombre
        CustomTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = "Nombre Completo"
        )

        // Campo de correo electrónico
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "Correo Electrónico"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para guardar cambios
        Button(
            onClick = {
                profileViewModel.updateProfile(name, email, selectedImage) // Método del ViewModel
                onSaveChanges() // Navegar de vuelta al perfil
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextBrown),
            shape = CircleShape
        ) {
            Text(
                text = "Guardar Cambios",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Botón para cancelar la edición
        Button(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = CircleShape
        ) {
            Text(
                text = "Cancelar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Indicador de carga
        if (isLoading.value) {
            CircularProgressIndicator(color = TextBrown)
        }

        // Mensaje de error
        errorMessage.value?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
