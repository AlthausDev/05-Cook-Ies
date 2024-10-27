package com.althaus.dev.cookIes

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.althaus.dev.cookIes.ui.home.HomeView
import com.althaus.dev.cookIes.ui.login.LoginView
import com.althaus.dev.cookIes.ui.profile.ProfileView
import com.althaus.dev.cookIes.ui.signup.SignUpView
import com.althaus.dev.cookIes.ui.startup.StartUpView
import com.google.firebase.auth.FirebaseAuth

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
    auth: FirebaseAuth
) {
    // Redirige al usuario automáticamente si ya está autenticado
    val startDestination = if (auth.currentUser != null) Screen.Home.route else Screen.StartUp.route

//    NavHost(navController = navHostController, startDestination = startDestination) {
//        composable(Screen.StartUp.route) {
//            StartUpView(
//                navigateToLogin = { navHostController.navigate(Screen.Login.route) },
//                navigateToSignUp = { navHostController.navigate(Screen.SignUp.route) }
//            )
//        }
//        composable(Screen.Login.route) {
//            LoginView(auth) { navHostController.navigate(Screen.Home.route) }
//        }
//        composable(Screen.SignUp.route) {
//            SignUpView(auth) { navHostController.navigate(Screen.Home.route) }
//        }
//        composable(Screen.Home.route) {
//            HomeView(
//                navigateToProfile = { navHostController.navigate(Screen.Profile.route) }
//            )
//        }
//        composable(Screen.Profile.route) {
//            ProfileView()
//        }
//    }
}
