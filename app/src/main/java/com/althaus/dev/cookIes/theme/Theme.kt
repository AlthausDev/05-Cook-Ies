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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp

// =======================================
// Esquema de colores
// =======================================

/** Esquema de colores para el tema claro. */
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

/** Esquema de colores para el tema oscuro. */
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

/** Definición de las formas personalizadas para la aplicación. */
val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// =======================================
// Tema principal
// =======================================

/**
 * Configura el tema principal de la aplicación.
 *
 * @param userDarkTheme Indica si el usuario fuerza un tema específico. Si es `null`, se usa el tema del sistema.
 * @param content Contenido Composable que se renderiza dentro del tema.
 */
@Composable
fun CookIesTheme(
    userDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (userDarkTheme) {
        true -> true
        false -> false
        null -> isSystemInDarkTheme()
    }

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

/**
 * Crea un fondo degradado animado que adapta sus colores al esquema de colores del tema actual.
 *
 * @param modifier Modificador opcional para personalizar el fondo.
 * @param content Contenido Composable que se renderiza dentro del fondo.
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

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
