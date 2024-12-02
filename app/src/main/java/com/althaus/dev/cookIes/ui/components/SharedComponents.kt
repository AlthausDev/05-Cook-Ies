package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R


/**
 * Composable para mostrar el logotipo de la aplicación.
 *
 * Este componente muestra una imagen del logotipo, centrada y con un tamaño fijo.
 */
@Composable
fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}

/**
 * Composable que muestra un título principal y un subtítulo.
 *
 * @param title Título principal que se muestra en la parte superior.
 * @param subtitle Subtítulo que se muestra debajo del título.
 */
@Composable
fun TitleAndSubtitle(title: String, subtitle: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )
}

/**
 * Composable que muestra un texto clicable.
 *
 * @param text Texto que se mostrará.
 * @param onClick Lambda que se ejecuta cuando el texto es clicado.
 */
@Composable
fun ClickableText(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable { onClick() },
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}

/**
 * Composable para mostrar un mensaje de error.
 *
 * @param message Mensaje que describe el error.
 */
@Composable
fun SharedErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Composable para un botón primario con opciones de personalización.
 *
 * @param text Texto del botón.
 * @param onClick Lambda que se ejecuta al hacer clic en el botón.
 * @param modifier Modificador para el diseño del botón.
 * @param icon Ícono opcional que se muestra junto al texto.
 * @param backgroundColor Color de fondo del botón.
 * @param contentColor Color del texto y los íconos dentro del botón.
 * @param borderColor Color del borde del botón.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(0.8f)
            .border(1.dp, borderColor, shape = CircleShape)
            .background(backgroundColor, shape = CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Composable para un campo de texto personalizado.
 *
 * Este campo admite entrada de texto simple o contraseñas, con opciones de personalización
 * para colores, bordes, transformaciones de texto y más.
 *
 * @param value Texto actual del campo.
 * @param onValueChange Lambda que se ejecuta cuando el texto cambia.
 * @param placeholder Texto que se muestra cuando el campo está vacío.
 * @param isPassword Indica si el campo es para entrada de contraseñas.
 * @param fieldWidth Ancho del campo de texto como un porcentaje del ancho total.
 * @param paddingValues Espaciado interno del campo.
 * @param textColor Color del texto.
 * @param placeholderColor Color del texto del placeholder.
 * @param backgroundColor Color de fondo del campo.
 * @param borderColor Color del borde del campo.
 * @param visualTransformation Transformación visual del texto (por ejemplo, contraseñas).
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    fieldWidth: Float = 0.8f,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    placeholderColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    backgroundColor: Color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    visualTransformation: VisualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth(fieldWidth)
            .border(2.dp, borderColor, CircleShape)
            .background(backgroundColor, CircleShape)
            .padding(paddingValues),
        singleLine = true,
        visualTransformation = visualTransformation,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = textColor
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = placeholderColor
                    )
                }
                innerTextField()
            }
        }
    )
}


/**
 * Composable para una barra superior compartida.
 *
 * @param title Título que se muestra en la barra.
 * @param actions Composables adicionales que se colocan como acciones en la barra.
 * @param navigationIcon Icono opcional para navegación en la barra.
 * @param backgroundColor Color de fondo de la barra.
 * @param contentColor Color del contenido de la barra (título y acciones).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTopAppBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = null, // Sigue siendo opcional
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor
        )
    )
}

/**
 * Composable para un botón de acción flotante compartido.
 *
 * @param onClick Lambda que se ejecuta al hacer clic en el botón.
 * @param icon Vector gráfico que se muestra dentro del botón.
 */
@Composable
fun SharedFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Composable para un indicador de carga compartido.
 *
 * @param size Tamaño del indicador de carga en píxeles.
 */
@Composable
fun SharedLoadingIndicator(size: Int = 48) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = (size / 12f).dp
        )
    }
}
