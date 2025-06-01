package com.example.systemaplikacia100.ui.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen() {
    // Získame aktuálne prihláseného Firebase používateľa
    val currentUser = Firebase.auth.currentUser

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (currentUser != null) {
            Text(
                text = "Vitajte, ${currentUser.email}! 🎉",
                style = MaterialTheme.typography.headlineMedium
            )
        } else {
            Text(
                text = "Vitajte v aplikácii!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
