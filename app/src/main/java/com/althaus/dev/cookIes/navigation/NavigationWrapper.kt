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

sealed class Screen(val route: String) {
    object StartUp : Screen("startUp")
    object Login : Screen("logIn")
    object SignUp : Screen("signUp")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object Wizard : Screen("wizard")
    object Favorites : Screen("favorites")
    object ForgotPassword : Screen("forgotPassword")
    object RecipeDetail : Screen("recipeDetail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipeDetail/$recipeId"
    }
}

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    recipeViewModel: RecipeViewModel,
    notificationsViewModel: NotificationsViewModel,
    firestoreRepository: FirestoreRepository
) {
    val currentUser by authViewModel.user.collectAsState()
    val startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.StartUp.route

    // Redirigir según el estado del usuario autenticado
    LaunchedEffect(currentUser) {
        val destination = if (currentUser != null) Screen.Dashboard.route else Screen.StartUp.route
        navigateWithClearBackStack(navHostController, destination)
    }

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
                    navHostController.navigate(Screen.RecipeDetail.createRoute(recipeId))
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
                onSave = { navHostController.popBackStack() },
                onCancel = { navHostController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navigateWithClearBackStack(navHostController, Screen.StartUp.route)
                }
            )
        }

        composable(Screen.Wizard.route) {
            RecipeWizardView(
                firestoreRepository = firestoreRepository,
                navHostController = navHostController,
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
                onFavorite = { /* Acción para favoritos */ },
                onShare = { /* Acción para compartir */ }
            )
        }
    }
}

private fun navigateWithClearBackStack(navController: NavHostController, destination: String) {
    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}
