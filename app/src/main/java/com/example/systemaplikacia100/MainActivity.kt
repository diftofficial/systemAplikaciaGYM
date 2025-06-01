package com.example.systemaplikacia100

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// TU sú importy tvojich vlastných balíčkov: sled za balíčkom “com.example.systemaplikacia100”
import com.example.systemaplikacia100.ui.navigation.NavGraph
import com.example.systemaplikacia100.ui.theme.SystemAplikacia100Theme  // ak je tvoja téma takto pomenovaná
import com.example.systemaplikacia100.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SystemAplikacia100Theme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                // Skontroluje, či užívateľ je prihlásený (FirebaseAuth)
                val startDest = if (Firebase.auth.currentUser != null) "home" else "login"

                NavGraph(
                    navController = navController,
                    startDestination = startDest,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
