// src/main/java/com/example/systemaplikacia100/ui/screens/UserScreen.kt

package com.example.systemaplikacia100.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun userScreen() {
    // 1) Získame aktuálneho prihláseného Firebase používateľa
    val currentUser = Firebase.auth.currentUser

    // 2) Stavová premenna pre body (points). Načítame z Firestore.
    var userPoints by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // 3) Asynchrónne načítanie bodov len raz, pri prvej kompozícii.
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            try {
                // Čítame dokument z kolekcie "users" pre tento uid
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                // Ak existuje, načítame pole "points"
                val pts = snapshot.getLong("points") ?: 0L
                userPoints = pts
            } catch (e: Exception) {
                // Ak sa niečo pokazí, ponecháme userPoints = 0
                userPoints = 0L
            }
        } ?: run {
            // Ak používateľ nie je prihlásený, nastavíme hodnotu na 0
            userPoints = 0L
        }
    }

    // 4) Vizuálna časť
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 4.a) Text privítania v strede obrazovky
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (currentUser != null) {
                Text(
                    text = "Vitajte, ${currentUser.email} ! 🎉",
                    style = MaterialTheme.typography.headlineMedium
                )
            } else {
                Text(
                    text = "Vitajte v aplikácii!",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        // 4.b) Text “Body: XX” v pravom hornom rohu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, end = 30.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = "Body: ${userPoints ?: 0L}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
