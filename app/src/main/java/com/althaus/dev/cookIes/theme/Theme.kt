package com.althaus.dev.cookIes.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp

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

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    // Animar los colores para transiciones suaves
    val animatedSurfaceColor = animateColorAsState(targetValue = surfaceColor)
    val animatedBackgroundColor = animateColorAsState(targetValue = backgroundColor)

    val gradientColors = listOf(
        animatedSurfaceColor.value,
        animatedBackgroundColor.value
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors,
                    tileMode = TileMode.Clamp
                )
            )
    ) {
        content()
    }
}
