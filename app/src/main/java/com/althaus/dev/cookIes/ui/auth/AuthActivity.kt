package com.althaus.dev.cookIes.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.althaus.dev.cookIes.ui.auth.ui.theme.Project05RecetarioTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AuthActivity : ComponentActivity() {
    private var email by mutableStateOf("")
    private var password by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Project05RecetarioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthScreen()
                }
            }
        }
    }

    @Composable
    fun AuthScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") }
            )
            Button(onClick = { /* Handle login */ }) {
                Text("Login")
            }
            Button(onClick = { /* Handle register */ }) {
                Text("Register")
            }
        }
    }
}
