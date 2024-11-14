package com.althaus.dev.cookIes.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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
private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = TextPrimary,
    secondary = SecondaryGray,
    onSecondary = Color.White,
    background = PrimaryLight,
    surface = PrimaryDark,
    onSurface = TextPrimary,
    error = Color.Red
)

// Esquema de color oscuro (opcional)
private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    secondary = SecondaryGray,
    onSecondary = Color.White,
    background = SecondaryBlack,
    surface = PrimaryDark,
    onSurface = Color.White,
    error = Color.Red
)

val AppShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

// Tema principal de la aplicación
@Composable
fun CookIesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

// Estilo de gradiente de fondo
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryLight, PrimaryDark),
                    tileMode = TileMode.Clamp
                )
            )
        ) {
            content()
        }
    }
}

// Estilos predeterminados de botones
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
        colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
        shape = CircleShape
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}
