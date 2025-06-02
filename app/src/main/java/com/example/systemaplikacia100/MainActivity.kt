// MainActivity.kt
package com.example.systemaplikacia100

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.systemaplikacia100.ui.navigation.NavGraph
import com.example.systemaplikacia100.ui.theme.SystemAplikacia100Theme
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SystemAplikacia100Theme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                // **VŽDY** začíname na "splash" (a tá rozhodne, či skočí na login alebo home)
                NavGraph(
                    navController    = navController,
                    startDestination = "splash",
                    authViewModel    = authViewModel
                )
            }
        }
    }
}
