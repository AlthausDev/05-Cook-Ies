package com.althaus.dev.cookIes.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.althaus.dev.cookIes.ui.dashboard.DashboardView
import com.althaus.dev.cookIes.ui.authentication.LoginView
import com.althaus.dev.cookIes.ui.profile.ProfileView
import com.althaus.dev.cookIes.ui.authentication.SignUpView
import com.althaus.dev.cookIes.ui.authentication.StartUpView
import com.althaus.dev.cookIes.ui.notifications.NotificationsView
import com.althaus.dev.cookIes.ui.settings.SettingsView
import com.althaus.dev.cookIes.ui.favorites.FavoritesView
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
    object Favorites : Screen("favorites")
}

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    recipeViewModel: RecipeViewModel
) {
    val currentUser = authViewModel.user.collectAsState().value
    val startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.StartUp.route

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
            authViewModel.resetError()
            DashboardView(
                recipeViewModel = recipeViewModel,
                navigateToRecipeDetail = { recipeId ->
                    authViewModel.resetError()
                    navHostController.navigate("recipeDetail/$recipeId")
                },
                navigateToProfile = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.Profile.route)
                },
                navigateToNotifications = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.Notifications.route)
                }
//                onRecipeClick = { recipe ->
//                    authViewModel.resetError()
//                    navHostController.navigate("recipeDetail/${recipe.id}")
//                }
            )
        }

        composable(Screen.Profile.route) {
            authViewModel.resetError()
            ProfileView(
                onEditProfile = {
                    authViewModel.resetError()
                    navHostController.navigate(Screen.Settings.route)
                },
                onLogout = {
                    authViewModel.resetError()
                    authViewModel.logout()
                    navigateWithClearBackStack(navHostController, Screen.StartUp.route)
                },
                profileViewModel = profileViewModel,
                onRecipeClick = { recipeId ->
                    authViewModel.resetError()
                    navHostController.navigate("recipeDetail/$recipeId")
                }
            )
        }

        composable(Screen.Notifications.route) {
            authViewModel.resetError()
            NotificationsView(
                onBack = {
                    authViewModel.resetError()
                    navHostController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            authViewModel.resetError()
            SettingsView(
                onBack = {
                    authViewModel.resetError()
                    navHostController.popBackStack()
                }
            )
        }

//        composable(Screen.Favorites.route) {
//            authViewModel.resetError()
//            FavoritesView(
//                onRecipeClick = { recipeId ->
//                    authViewModel.resetError()
//                    navHostController.navigate("recipeDetail/$recipeId")
//                },
//                recipeViewModel = recipeViewModel
//            )
//        }
    }
}

private fun navigateWithClearBackStack(navController: NavHostController, destination: String) {
    navController.navigate(destination) {
        popUpTo(navController.graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}
