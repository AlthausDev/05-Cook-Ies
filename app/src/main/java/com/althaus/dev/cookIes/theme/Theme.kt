package com.althaus.dev.cookIes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.ui.components.CustomTextField
import com.althaus.dev.cookIes.ui.components.PrimaryButton

// =======================================
// Esquema de colores
// =======================================

// Esquema de colores para el tema claro
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = LightError,
    onError = LightOnError
)

// Esquema de colores para el tema oscuro
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError
)

// =======================================
// Formas personalizadas
// =======================================
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// =======================================
// Tema principal
// =======================================
@Composable
fun CookIesTheme(
    userDarkTheme: Boolean? = null, // null para usar el tema del sistema
    content: @Composable () -> Unit
) {
    // Determina si usar el tema oscuro o claro
    val darkTheme = when (userDarkTheme) {
        true -> true // Usuario fuerza tema oscuro
        false -> false // Usuario fuerza tema claro
        null -> isSystemInDarkTheme() // Usa el tema del sistema
    }

    // Selecciona el esquema de color según el tema
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = AppShapes
    ) {
        content()
    }
}

// =======================================
// Componentes comunes
// =======================================

// Fondo dinámico con gradiente (para usar en cualquier pantalla)
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.background
    )

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = gradientColors,
                tileMode = TileMode.Clamp
            )
        )
    ) {
        content()
    }
}

@Preview(name = "CustomTextField - Light Theme", showBackground = true)
@Composable
fun PreviewCustomTextFieldLight() {
    CookIesTheme(userDarkTheme = false) { // Forzar tema claro
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CustomTextField(
                value = "",
                onValueChange = {},
                placeholder = "Nombre"
            )
            CustomTextField(
                value = "",
                onValueChange = {},
                placeholder = "Contraseña",
                isPassword = true
            )
        }
    }
}

@Preview(name = "CustomTextField - Dark Theme", showBackground = true)
@Composable
fun PreviewCustomTextFieldDark() {
    CookIesTheme(userDarkTheme = true) { // Forzar tema oscuro
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            CustomTextField(
                value = "",
                onValueChange = {},
                placeholder = "Nombre"
            )
            CustomTextField(
                value = "",
                onValueChange = {},
                placeholder = "Contraseña",
                isPassword = true
            )
        }
    }
}
