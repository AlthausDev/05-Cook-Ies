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
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {

//    NavHost(navController = navHostController, startDestination = "home") {
//        composable("startUp") {
//            StartUpView(navigateToLogin = { navHostController.navigate("logIn") },
//                navigateToSignUp = { navHostController.navigate("signUp") })
//        }
//        composable("logIn") {
//            LoginView(auth){ navHostController.navigate("home") }
//        }
//        composable("signUp") {
//            SignUpView(auth)
//        }
//        composable("home"){
//            HomeView()
//        }
//        composable("profile"){
//            ProfileView()
//        }
//    }
}