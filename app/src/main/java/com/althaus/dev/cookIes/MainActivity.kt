package com.althaus.dev.cookIes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.althaus.dev.cookIes.ui.navigation.NavigationWrapper
import com.althaus.dev.cookIes.ui.theme.CookIesTheme
import com.althaus.dev.cookIes.viewmodel.AuthViewModel
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel
import com.google.android.gms.security.ProviderInstaller
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ProviderInstaller para asegurar actualizaciones SSL
        ProviderInstaller.installIfNeeded(applicationContext)

        setContent {
            val navController = rememberNavController()
            CookIesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pasamos todos los ViewModels necesarios a NavigationWrapper
                    NavigationWrapper(
                        navHostController = navController,
                        authViewModel = authViewModel,
                        profileViewModel = profileViewModel,
                        recipeViewModel = recipeViewModel
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROVIDER_INSTALL_REQUEST_CODE) {
            // Si el proveedor fue instalado correctamente o falló, puedes manejar el resultado aquí
            Log.d("ProviderInstaller", "Resultado de ProviderInstaller recibido con código: $resultCode")
        }
    }

    companion object {
        private const val PROVIDER_INSTALL_REQUEST_CODE = 1
    }
}
