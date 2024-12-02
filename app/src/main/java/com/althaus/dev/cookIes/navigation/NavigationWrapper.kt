package com.althaus.dev.cookIes.navigation

import NotificationsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.althaus.dev.cookIes.data.repository.FirestoreRepository
import com.althaus.dev.cookIes.ui.authentication.ForgotPasswordView
import com.althaus.dev.cookIes.ui.authentication.LoginView
import com.althaus.dev.cookIes.ui.authentication.SignUpView
import com.althaus.dev.cookIes.ui.authentication.StartUpView
import com.althaus.dev.cookIes.ui.dashboard.DashboardView
import com.althaus.dev.cookIes.ui.favorites.FavoritesView
import com.althaus.dev.cookIes.ui.notifications.NotificationsView
import com.althaus.dev.cookIes.ui.profile.ProfileView
import com.althaus.dev.cookIes.ui.recipe.RecipeDetailView
import com.althaus.dev.cookIes.ui.recipe.RecipeWizardView
import com.althaus.dev.cookIes.ui.settings.SettingsView
import com.althaus.dev.cookIes.viewmodel.AuthViewModel
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * Clase que define las rutas de las diferentes pantallas de la aplicación.
 *
 * Cada pantalla tiene una ruta asociada que se utiliza para la navegación.
 *
 * @property route La ruta asociada a la pantalla.
 */
sealed class Screen(val route: String) {
    /** Pantalla de inicio. */
    object StartUp : Screen("startUp")

    /** Pantalla de inicio de sesión. */
    object Login : Screen("logIn")

    /** Pantalla de registro. */
    object SignUp : Screen("signUp")

    /** Pantalla principal del tablero. */
    object Dashboard : Screen("dashboard")

    /** Pantalla de perfil del usuario. */
    object Profile : Screen("profile")

    /** Pantalla de notificaciones. */
    object Notifications : Screen("notifications")

    /** Pantalla de configuración. */
    object Settings : Screen("settings")

    /** Pantalla para el asistente de creación de recetas. */
    object Wizard : Screen("wizard")

    /** Pantalla de favoritos del usuario. */
    object Favorites : Screen("favorites")

    /** Pantalla para recuperación de contraseña. */
    object ForgotPassword : Screen("forgotPassword")

    /**
     * Pantalla de detalles de una receta específica.
     *
     * @param recipeId Identificador único de la receta.
     */
    object RecipeDetail : Screen("recipeDetail/{recipeId}") {
        /**
         * Genera una ruta específica para acceder a los detalles de una receta.
         *
         * @param recipeId El identificador único de la receta.
         * @return La ruta para navegar a la pantalla de detalles de la receta.
         */
        fun createRoute(recipeId: String) = "recipeDetail/$recipeId"
    }
}

/**
 * Configura y gestiona el sistema de navegación de la aplicación.
 *
 * Este composable centraliza las rutas de navegación y define el comportamiento
 * de las transiciones entre pantallas.
 *
 * @param navHostController Controlador de navegación utilizado para manejar las rutas.
 * @param authViewModel ViewModel de autenticación para gestionar el estado del usuario.
 * @param profileViewModel ViewModel del perfil del usuario.
 * @param recipeViewModel ViewModel de recetas.
 * @param notificationsViewModel ViewModel de notificaciones.
 * @param firestoreRepository Repositorio de Firestore para acceso a datos.
 * @param onToggleTheme Callback para alternar entre temas claro y oscuro.
 */
@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    recipeViewModel: RecipeViewModel,
    notificationsViewModel: NotificationsViewModel,
    firestoreRepository: FirestoreRepository,
    onToggleTheme: () -> Unit
) {
    val currentUser by authViewModel.user.collectAsState()

    // Define la pantalla inicial según el estado de autenticación del usuario
    val startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.StartUp.route

    // Efecto para redirigir automáticamente si el estado del usuario cambia
    LaunchedEffect(currentUser) {
        val destination = if (currentUser != null) Screen.Dashboard.route else Screen.StartUp.route
        navigateWithClearBackStack(navHostController, destination)
    }

    // Configuración del host de navegación con las rutas definidas
    NavHost(navController = navHostController, startDestination = startDestination) {
        composable(Screen.StartUp.route) {
            StartUpView(
                navigateToLogin = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.Login.route)
                },
                navigateToSignUp = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.SignUp.route)
                },
                authViewModel = authViewModel,
                onLoginSuccess = {
                    authViewModel.resetError()
                    navigateWithClearBackStack(navHostController, Screen.Dashboard.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginView(
                navigateToSignUp = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    authViewModel.resetError()
                    navigateWithClearBackStack(navHostController, Screen.Dashboard.route)
                },
                navigateToForgotPassword = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.ForgotPassword.route)
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.SignUp.route) {
            SignUpView(
                navigateToLogin = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.Login.route)
                },
                onSignUpSuccess = {
                    authViewModel.resetError()
                    navigateWithClearBackStack(navHostController, Screen.Dashboard.route)
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardView(
                recipeViewModel = recipeViewModel,
                navigateToRecipeDetail = { recipeId ->
                    navHostController.navigate("recipeDetail/$recipeId")
                },
                navigateToProfile = {
                    navHostController.navigate(Screen.Profile.route)
                },
                navigateToNotifications = {
                    navHostController.navigate(Screen.Notifications.route)
                },
                navigateToRecipeWizard = {
                    navHostController.navigate(Screen.Wizard.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileView(
                onSettings = {
                    navHostController.navigate(Screen.Settings.route)
                },
                profileViewModel = profileViewModel,
                onRecipeClick = { recipeId ->
                    navHostController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                navigateToFavorites = {
                    navHostController.navigate(Screen.Favorites.route)
                },
                onBack = {
                    navHostController.popBackStack()
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsView(
                notificationsViewModel = notificationsViewModel,
                onBack = { navHostController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsView(
                profileViewModel = profileViewModel,
                onCancel = { navHostController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navigateWithClearBackStack(navHostController, Screen.StartUp.route)
                },
                onToggleTheme = onToggleTheme // Pasamos el callback de alternancia de tema
            )
        }

        composable(Screen.Wizard.route) {
            RecipeWizardView(
                firestoreRepository = firestoreRepository,
                navHostController = navHostController,
                currentAuthorId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown-user",
                onComplete = {
                    navHostController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onCancel = { navHostController.popBackStack() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesView(
                recipeViewModel = recipeViewModel,
                onRecipeClick = { recipeId ->
                    navHostController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onBack = { navHostController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordView(
                authViewModel = authViewModel,
                onBack = { navHostController.popBackStack() }
            )
        }

        composable(Screen.RecipeDetail.route) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailView(
                viewModel = recipeViewModel.apply { getRecipeById(recipeId) },
                onBack = { navHostController.popBackStack() },
                onFavorite = { /* Acción para favoritos */ }
            )
        }
    }
}

/**
 * Navega a una ruta específica limpiando el backstack.
 *
 * Este método garantiza que la navegación limpia el historial de rutas para evitar
 * comportamientos indeseados al regresar.
 *
 * @param navController Controlador de navegación utilizado para manejar las rutas.
 * @param destination Ruta de destino a la que se desea navegar.
 */
private fun navigateWithClearBackStack(navController: NavHostController, destination: String) {
    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}
