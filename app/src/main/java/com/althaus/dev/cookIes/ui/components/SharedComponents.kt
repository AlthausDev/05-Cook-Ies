package com.althaus.dev.cookIes.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.theme.TextPrimary

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
        color = TextPrimary,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Text(
        text = subtitle,
        color = TextPrimary,
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
        color = TextPrimary,
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
fun CustomButton(
    modifier: Modifier = Modifier,
    painter: Painter,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .background(Color.White, shape = CircleShape)
            .border(1.dp, TextPrimary, shape = CircleShape)
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 8.dp)
        )
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LoadingIndicator() {
    CircularProgressIndicator(color = TextPrimary)
}
