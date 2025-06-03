// AdminHomeScreen.kt

package com.example.systemaplikacia100.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.systemaplikacia100.R
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * AdminScreen / AdminHomeScreen
 *
 * Parametre:
 *  - navController: na navigáciu späť na login
 *  - authViewModel: ak potrebuješ logiku z AuthViewModelu (napr. načítanie používateľa, body atď.)
 *
 * Obrazovka umožňuje zadať "meno_pouzivatela" a "+ pocet_kreditov". Po kliknutí na tlačidlo
 * sa pripočítajú body do Firestore v kolekcii "users", field "points" pre dané username.
 * Na spätnú väzbu používame jednoduchý Toast.
 */
@Composable
fun adminScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {

    var username by rememberSaveable { mutableStateOf("") }
    var credits by rememberSaveable { mutableStateOf("") }

    // Stav loadingu
    var isLoading by rememberSaveable { mutableStateOf(false) }

    // Potrebujeme kontext pre zobrazenie Toast
    val context = LocalContext.current

    // Coroutine scope pre suspend volania (Firestore kód)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // tmavé pozadie celej obrazovky
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --------------------------------------------------
            // 1) Tlačidlo "Odhlásiť sa"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        // Odhlásime sa z Firebase Auth a vrátime sa na "login" obrazovku
                        Firebase.auth.signOut()
                        navController.navigate("login") {
                            popUpTo("adminHome") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(text = "Odhlásiť sa", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --------------------------------------------------
            // 2) Nadpis
            Text(
                text = "Admin",
                color = Color(0xFFFFEB3B),
                fontSize = 50.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "pridávanie kreditov",
                color = Color(0xFFFFEB3B),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(100.dp))

            // --------------------------------------------------
            // 3) Box + BasicTextField: zadaj "meno_pouzivatela"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (username.isEmpty()) {
                    Text(
                        text = "email_pouzivatela",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --------------------------------------------------
            // 4) Box + BasicTextField: zadaj "+ pocet_kreditov"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (credits.isEmpty()) {
                    Text(
                        text = "+ pocet_kreditov",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = credits,
                    onValueChange = { credits = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --------------------------------------------------
            // 5) Tlačidlo "potvrdit" – tu voláme Firestore aktualizáciu
            Button(
                onClick = {
                    coroutineScope.launch {
                        val nameTrimmed = username.trim()
                        val creditsTrimmed = credits.trim()

                        // Validácia vstupov
                        if (nameTrimmed.isEmpty() || creditsTrimmed.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Zadaj meno aj počet kreditov",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        // Konverzia credits na Long
                        val newPoints = creditsTrimmed.toLongOrNull()
                        if (newPoints == null) {
                            Toast.makeText(
                                context,
                                "Počet kreditov musí byť číslo",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        // Zobrazíme loading indikátor
                        isLoading = true
                        try {
                            addPointsToUser(nameTrimmed, newPoints)
                            Toast.makeText(
                                context,
                                "Body úspešne pridané",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Po úspechu vyčistíme polia
                            username = ""
                            credits = ""
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Chyba pri pridávaní bodov: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEB3B),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoading) "Pracujem..." else "potvrdit",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --------------------------------------------------
            // 6) Spinner, ak sa čaká na Firestore
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFFFFEB3B))
            }
        }

        // --------------------------------------------------
        // 7) Logo v pravom dolnom rohu
        // Uprav názov drawable, ak sa volá inak (napr. gym_logo.png)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo GYM",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(80.dp)
        )
    }
}

/**
 * Suspend funkcia, ktorá nájde v Firestore dokumenty v kolekcii "users"
 * kde field "username" == username. Ak existuje, zoberie pole "points",
 * pripočíta k nemu pointsToAdd a urobí update.
 */
suspend fun addPointsToUser(username: String, pointsToAdd: Long) {
    val db = FirebaseFirestore.getInstance()

    // 1) Query podľa field "username"
    val querySnapshot = db.collection("users")
        .whereEqualTo("email", username)
        .get()
        .await()

    if (querySnapshot.isEmpty) {
        throw Exception("Používateľ '$username' neexistuje.")
    }

    // Predpokladáme, že username je unikátne – vezmeme prvý dokument
    val doc = querySnapshot.documents[0]
    val docRef = doc.reference

    // 2) Načítame existujúce body (alebo 0, ak pole chýba)
    val currentPoints: Long = doc.getLong("points") ?: 0L

    // 3) Spočítame a uložíme späť do Firestore
    val updatedPoints = currentPoints + pointsToAdd
    docRef.update("points", updatedPoints).await()
}
