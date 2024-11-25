package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R

@Composable
fun AppLogo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}

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

@Composable
fun ErrorText(message: String) {
    Text(
        text = message,
        color = Color.Red,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null, // Ícono opcional
    backgroundColor: Color = MaterialTheme.colorScheme.secondary, // Fondo por defecto
    contentColor: Color = MaterialTheme.colorScheme.primary, // Texto por defecto
    borderColor: Color = MaterialTheme.colorScheme.primary // Color del borde
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(0.8f)
            .border(1.dp, borderColor, shape = CircleShape) // Siempre aplica el borde
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
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    fieldWidth: Float = 0.8f,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    textColor: Color = MaterialTheme.colorScheme.onBackground, // Color del texto
    placeholderColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f), // Placeholder
    backgroundColor: Color = MaterialTheme.colorScheme.secondary, // Fondo
    borderColor: Color = MaterialTheme.colorScheme.primary, // Borde
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

@Composable
fun LoadingIndicator() {
    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
}
