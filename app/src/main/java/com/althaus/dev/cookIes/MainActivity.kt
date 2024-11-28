package com.althaus.dev.cookIes

import NotificationsViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ViewModels necesarios para la navegación
    private val authViewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()

    @Inject
    lateinit var firestoreRepository: FirestoreRepository // Inyección del repositorio Firestore

    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Asegurar que SSL esté actualizado
        ProviderInstaller.installIfNeeded(applicationContext)

        setContent {
            navController = rememberNavController() // Controlador de navegación

            // Estado del tema inicializado en modo claro (false)
            val isDarkTheme = remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            CookIesTheme(userDarkTheme = isDarkTheme.value) { // Aplica el tema
                Surface {
                    NavigationWrapper(
                        navHostController = navController,
                        authViewModel = authViewModel,
                        profileViewModel = profileViewModel,
                        recipeViewModel = recipeViewModel,
                        notificationsViewModel = NotificationsViewModel(firestoreRepository),
                        firestoreRepository = firestoreRepository,
                        onToggleTheme = {
                            scope.launch {
                                // Alternar entre modo claro y oscuro
                                isDarkTheme.value = !isDarkTheme.value
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROVIDER_INSTALL_REQUEST_CODE) {
            Log.d("ProviderInstaller", "Resultado de ProviderInstaller recibido con código: $resultCode")
        }
    }

    companion object {
        private const val PROVIDER_INSTALL_REQUEST_CODE = 1
    }
}
