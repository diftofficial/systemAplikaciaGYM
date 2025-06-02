// napr. src/main/java/com/example/systemaplikacia100/ui/navigation/NavGraph.kt

package com.example.systemaplikacia100.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.systemaplikacia100.ui.screens.*
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        // 1) Splash: vždy prvá, rozhoduje, kam ďalej
        composable("splash") {
            SplashScreen(navController)
        }

        // 2) Login / Register
        composable("login") {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }

        // 3) Home obrazovky podľa rolí
        composable("userHome") {
            userScreen()
        }
        composable("adminHome") {
            adminScreen(     // <-- tu už Editor nájde tvoj AdminScreen
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("trainerHome") {
            trainerScreen()  // nová obrazovka pre „trainer“
        }
    }
}

/**
 * SplashScreen – pri štarte:
 *   • ak nie je nikto prihlásený → skočí na "login"
 *   • ak existuje currentUser → načíta z Firestore pole "role"
 *       • ak "admin"   → "adminHome"
 *       • ak "trainer" → "trainerHome"
 *       • inak        → "userHome"
 */
@Composable
private fun SplashScreen(navController: NavHostController) {
    // 1) UI: spinner v strede
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    // 2) Logika: ihneď po spustení načíta stav
    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // NIKTO nie je prihlásený → login
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // UŽÍVATEĽ je prihlásený → načítame “role” z Firestore (cache)
            try {
                val snapshot = Firebase.firestore
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                val roleFromDb = snapshot.getString("role")
                when (roleFromDb) {
                    "admin" -> {
                        navController.navigate("adminHome") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    "trainer" -> {
                        navController.navigate("trainerHome") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                    else -> {
                        // predvolená úroveň: "user"
                        navController.navigate("userHome") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // ak načítanie zlyhá, vrátime sa radšej na login
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }
}
