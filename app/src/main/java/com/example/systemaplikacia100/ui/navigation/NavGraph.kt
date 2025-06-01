package com.example.systemaplikacia100.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.systemaplikacia100.ui.screens.HomeScreen
import com.example.systemaplikacia100.ui.screens.LoginScreen
import com.example.systemaplikacia100.ui.screens.RegisterScreen
import com.example.systemaplikacia100.viewmodel.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController, startDestination: String, authViewModel: AuthViewModel) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = "login") {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable(route = "register") {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }
        composable(route = "home") {
            HomeScreen()
        }
    }
}
