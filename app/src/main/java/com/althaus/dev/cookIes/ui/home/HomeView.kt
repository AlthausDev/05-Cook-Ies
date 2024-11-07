package com.althaus.dev.cookIes.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.althaus.dev.cookIes.R
import com.althaus.dev.cookIes.ui.startup.ParchmentLight
import com.althaus.dev.cookIes.ui.startup.ParchmentDark
import com.althaus.dev.cookIes.ui.startup.TextBrown

@Preview
@Composable
fun HomeView(navigateToProfile: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ParchmentLight, ParchmentDark))),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(10) { index ->
            RecipeCard(
                title = "Receta ${index + 1}",
                description = "Una breve descripción de la receta ${index + 1} para que te inspires a cocinar algo delicioso."
            )
        }
    }
}

@Composable
fun RecipeCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Imagen de la receta (placeholder)
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Título de la receta
            Text(
                text = title,
                color = TextBrown,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Descripción de la receta
            Text(
                text = description,
                color = TextBrown.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}
