package com.althaus.dev.cookIes.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Esquema de color claro
private val ThemeColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryDark, // Texto/íconos sobre primary
    background = PrimaryLight,
    onBackground = PrimaryDark, // Texto sobre background
    surface = SecondaryLight,
    onSurface = PrimaryDark, // Texto sobre surface
    error = ErrorLight,
    onError = Color.White, // Texto sobre error
    secondary = SecondaryLight, // Color secundario
    onSecondary = PrimaryDark // Texto/íconos sobre secondary
)


// Esquema de color oscuro
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    error = DarkError,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onError = DarkOnError
)

// Formas personalizadas
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// Tema principal de la aplicación
@Composable
fun CookIesTheme(
    userDarkTheme: Boolean? = null, // null usa el tema por defecto del sistema
    content: @Composable () -> Unit
) {
    // Determina el tema actual (Light por defecto)
    val darkTheme = when (userDarkTheme) {
        true -> true // Usuario fuerza tema oscuro
        false -> false // Usuario fuerza tema claro
        null -> isSystemInDarkTheme() // Usa el tema del sistema
    }

    //val colors = if (darkTheme) DarkColorScheme else ThemeColors
    val colors = ThemeColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = AppShapes
    ) {
        content()
    }
}

// Fondo dinámico con gradiente
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.background
    )

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = colors,
                tileMode = TileMode.Clamp
            )
        )
    ) {
        content()
    }
}


// Estilo de botones
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = CircleShape
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}

