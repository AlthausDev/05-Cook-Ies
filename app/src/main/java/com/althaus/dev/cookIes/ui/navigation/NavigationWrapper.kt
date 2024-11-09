package com.althaus.dev.cookIes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.althaus.dev.cookIes.ui.home.HomeView
import com.althaus.dev.cookIes.ui.login.LoginView
import com.althaus.dev.cookIes.ui.profile.ProfileView
import com.althaus.dev.cookIes.ui.signup.SignUpView
import com.althaus.dev.cookIes.ui.startup.StartUpView
import com.althaus.dev.cookIes.viewmodel.AuthViewModel
import com.althaus.dev.cookIes.viewmodel.ProfileViewModel
import com.althaus.dev.cookIes.viewmodel.RecipeViewModel

sealed class Screen(val route: String) {
    object StartUp : Screen("startUp")
    object Login : Screen("logIn")
    object SignUp : Screen("signUp")
    object Home : Screen("home")
    object Profile : Screen("profile")
}

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    recipeViewModel: RecipeViewModel
) {
    val currentUser = authViewModel.user.collectAsState().value

    // Definir la pantalla de inicio basada en el estado de autenticación
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.StartUp.route

    // Lanzar efecto para redirigir cuando el estado de autenticación cambie
    LaunchedEffect(currentUser) {
        val destination = if (currentUser != null) Screen.Home.route else Screen.StartUp.route
        navigateWithClearBackStack(navHostController, destination)
    }

    NavHost(navController = navHostController, startDestination = startDestination) {
        composable(Screen.StartUp.route) {
            StartUpView(
                navigateToLogin = { navigateWithClearBackStack(navHostController, Screen.Login.route) },
                navigateToSignUp = { navigateWithClearBackStack(navHostController, Screen.SignUp.route) },
                authViewModel = authViewModel,
                onLoginSuccess = { navigateWithClearBackStack(navHostController, Screen.Home.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginView(
                navigateToSignUp = { navigateWithClearBackStack(navHostController, Screen.SignUp.route) },
                onLoginSuccess = { navigateWithClearBackStack(navHostController, Screen.Home.route) },
                authViewModel = authViewModel
            )
        }
        composable(Screen.SignUp.route) {
            SignUpView(
                navigateToLogin = { navigateWithClearBackStack(navHostController, Screen.Login.route) },
                onSignUpSuccess = { navigateWithClearBackStack(navHostController, Screen.Home.route) },
                authViewModel = authViewModel
            )
        }
        composable(Screen.Home.route) {
            HomeView(
                navigateToProfile = { navHostController.navigate(Screen.Profile.route) },
                onRecipeClick = { recipeId ->
                    // Aquí puedes navegar a una pantalla de detalles de la receta
                    // Por ejemplo:
                    // navHostController.navigate("recipeDetail/$recipeId")
                },
                recipeViewModel = recipeViewModel
            )
        }
        composable(Screen.Profile.route) {
            ProfileView(
                onEditProfile = { /* Aquí puedes manejar la navegación o acción de editar perfil */ },
                onLogout = {
                    authViewModel.logout()
                    navigateWithClearBackStack(navHostController, Screen.StartUp.route)
                },
                profileViewModel = profileViewModel,
                onRecipeClick = { recipeId ->
                    // Acción al hacer clic en una receta desde el perfil
                    // navHostController.navigate("recipeDetail/$recipeId")
                }
            )
        }
    }
}

// Función para navegación con limpieza de backstack
private fun navigateWithClearBackStack(navController: NavHostController, destination: String) {
    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}
