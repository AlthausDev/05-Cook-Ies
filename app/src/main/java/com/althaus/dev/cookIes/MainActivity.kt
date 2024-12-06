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

/**
 * Actividad principal de la aplicación.
 *
 * Esta clase actúa como punto de entrada principal de la aplicación. Configura el sistema
 * de navegación, el tema dinámico y asegura la compatibilidad SSL mediante la instalación
 * de actualizaciones de seguridad necesarias.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * ViewModel para la autenticación de usuarios.
     */
    private val authViewModel: AuthViewModel by viewModels()

    /**
     * ViewModel para gestionar el perfil del usuario.
     */
    private val profileViewModel: ProfileViewModel by viewModels()

    /**
     * ViewModel para la gestión de recetas.
     */
    private val recipeViewModel: RecipeViewModel by viewModels()

    /**
     * Repositorio Firestore para interactuar con la base de datos.
     *
     * Este repositorio se inyecta automáticamente mediante Hilt.
     */
    @Inject
    lateinit var firestoreRepository: FirestoreRepository

    /**
     * Controlador de navegación utilizado para gestionar las rutas de la aplicación.
     */
    private lateinit var navController: NavHostController

    /**
     * Método llamado cuando se crea la actividad.
     *
     * Configura el tema de la aplicación, el sistema de navegación y asegura
     * que las actualizaciones de seguridad SSL estén instaladas.
     *
     * @param savedInstanceState Estado guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Instala actualizaciones necesarias para SSL/TLS
        ProviderInstaller.installIfNeeded(applicationContext)

        setContent {
            navController = rememberNavController() // Inicializa el controlador de navegación

            // Estado mutable para alternar entre tema claro y oscuro
            val isDarkTheme = remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            // Aplica el tema actual
            CookIesTheme(userDarkTheme = isDarkTheme.value) {
                Surface { // Superficie para manejar el esquema de color
                    NavigationWrapper(
                        navHostController = navController,
                        authViewModel = authViewModel,
                        profileViewModel = profileViewModel,
                        recipeViewModel = recipeViewModel,
                        notificationsViewModel = NotificationsViewModel(firestoreRepository),
                        firestoreRepository = firestoreRepository,
                        onToggleTheme = {
                            scope.launch {
                                isDarkTheme.value = !isDarkTheme.value
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Maneja el resultado de actividades iniciadas para obtener resultados.
     *
     * @param requestCode Código de solicitud utilizado al iniciar la actividad.
     * @param resultCode Código de resultado devuelto por la actividad.
     * @param data Datos adicionales devueltos por la actividad, si existen.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROVIDER_INSTALL_REQUEST_CODE) {
            Log.d(
                "ProviderInstaller",
                "Resultado de ProviderInstaller recibido con código: $resultCode"
            )
        }
    }

    companion object {
        /**
         * Código de solicitud utilizado para instalar el proveedor de seguridad.
         */
        private const val PROVIDER_INSTALL_REQUEST_CODE = 1
    }
}
