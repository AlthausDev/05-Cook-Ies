package com.althaus.dev.cookIes

import NotificationsViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.navigation.NavigationWrapper
import com.althaus.dev.cookIes.theme.CookIesTheme
import com.althaus.dev.cookIes.viewmodel.AuthViewModel
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel
import com.google.android.gms.security.ProviderInstaller
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()

    @Inject
    lateinit var firestoreRepository: FirestoreRepository // Inyección de FirestoreRepository

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ProviderInstaller para asegurar actualizaciones SSL
        ProviderInstaller.installIfNeeded(applicationContext)

        setContent {
            navController = rememberNavController() // Inicializa el navController en el contexto de Compose
            CookIesTheme {
                Surface {

                    // Pasamos todos los ViewModels necesarios a NavigationWrapper
                    NavigationWrapper(
                        navHostController = navController,
                        authViewModel = authViewModel,
                        profileViewModel = profileViewModel,
                        recipeViewModel = recipeViewModel,
                        notificationsViewModel = NotificationsViewModel(firestoreRepository),
                        firestoreRepository = firestoreRepository // Pasamos el repositorio aquí
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
