package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.althaus.dev.cookIes.ui.startup.TextBrown
import androidx.compose.material3.Text

@Composable
fun CustomTextField(
    placeholder: String,
    isPassword: Boolean = false
) {
    var text by remember { mutableStateOf("") }

    BasicTextField(
        value = text,
        onValueChange = { newText -> text = newText },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(Color.White, CircleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (text.isEmpty()) {
                    Text(text = placeholder, color = TextBrown.copy(alpha = 0.6f))
                }
                innerTextField()
            }
        }
    )
}
