// src/main/java/com/example/systemaplikacia100/ui/screens/TrainerScreen.kt

package com.example.systemaplikacia100.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun trainerScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    // Stavové premenne pre vstupy
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var capacityInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }

    // Stav pre dátum a čas
    var selectedDateTime by remember { mutableStateOf<Calendar?>(null) }

    // Stav pri ukladaní
    var isSaving by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Pomocná funkcia na formátovanie Calendar → String
    fun calendarToDisplayString(cal: Calendar): String {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfWeekStr = when (dayOfWeekIndex) {
            Calendar.MONDAY -> "Pondelok"
            Calendar.TUESDAY -> "Utorok"
            Calendar.WEDNESDAY -> "Streda"
            Calendar.THURSDAY -> "Štvrtok"
            Calendar.FRIDAY -> "Piatok"
            Calendar.SATURDAY -> "Sobota"
            Calendar.SUNDAY -> "Nedeľa"
            else -> ""
        }
        return String.format(
            Locale.getDefault(),
            "%02d.%02d.%04d  %02d:%02d  (%s)",
            day, month, year, hour, minute, dayOfWeekStr
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { // --------------------------------------------------
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
            // Nadpis
            Text(
                text = "Vytvoriť nový tréning",
                fontSize = 35.sp,
                color = Color(0xFFFFEB3B)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Názov tréningu – BasicTextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (titleInput.isEmpty()) {
                    Text(
                        text = "Názov tréningu",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Popis tréningu – BasicTextField, viacriadkové
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (descriptionInput.isEmpty()) {
                    Text(
                        text = "Popis tréningu",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Dátum a čas – otvorenie DatePickerDialog + TimePickerDialog
            val calendarNow = Calendar.getInstance()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .clickable {
                        val initialCal = selectedDateTime ?: calendarNow
                        // DatePicker
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val tempCal = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth)
                                }
                                // TimePicker
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        tempCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        tempCal.set(Calendar.MINUTE, minute)
                                        tempCal.set(Calendar.SECOND, 0)
                                        tempCal.set(Calendar.MILLISECOND, 0)
                                        selectedDateTime = tempCal
                                    },
                                    initialCal.get(Calendar.HOUR_OF_DAY),
                                    initialCal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            initialCal.get(Calendar.YEAR),
                            initialCal.get(Calendar.MONTH),
                            initialCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (selectedDateTime == null) {
                    Text(
                        text = "Vyberte dátum a čas",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = calendarToDisplayString(selectedDateTime!!),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Kapacita – BasicTextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (capacityInput.isEmpty()) {
                    Text(
                        text = "Kapacita (počet miest)",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = capacityInput,
                    onValueChange = { capacityInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Cena v bodoch – BasicTextField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (priceInput.isEmpty()) {
                    Text(
                        text = "Cena v bodoch",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = priceInput,
                    onValueChange = { priceInput = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Tlačidlo "Uložiť termín"
            Button(
                onClick = {
                    coroutineScope.launch {
                        val title = titleInput.trim()
                        val desc = descriptionInput.trim()
                        val cal = selectedDateTime
                        val capText = capacityInput.trim()
                        val priceText = priceInput.trim()

                        if (title.isEmpty() || cal == null || capText.isEmpty() || priceText.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Vyplňte názov, dátum, kapacitu a cenu",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }
                        val capacityVal = capText.toLongOrNull()
                        val priceVal = priceText.toLongOrNull()
                        if (capacityVal == null || priceVal == null) {
                            Toast.makeText(
                                context,
                                "Kapacita a cena musia byť čísla",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        val currentTrainer = Firebase.auth.currentUser
                        val trainerId = currentTrainer?.uid ?: run {
                            Toast.makeText(
                                context,
                                "Nepodarilo sa získať ID trénera",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        val timestamp = Timestamp(cal.time)
                        isSaving = true
                        try {
                            val newSessionRef = FirebaseFirestore.getInstance()
                                .collection("sessions")
                                .document()
                            val sessionData = mapOf(
                                "trainerId" to trainerId,
                                "dateTime" to timestamp,
                                "capacity" to capacityVal,
                                "priceInPoints" to priceVal,
                                "participantsCount" to 0L,
                                "title" to title,
                                "description" to desc
                            )
                            newSessionRef.set(sessionData).await()

                            Toast.makeText(
                                context,
                                "Termín úspešne uložený",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Reset polí
                            titleInput = ""
                            descriptionInput = ""
                            selectedDateTime = null
                            capacityInput = ""
                            priceInput = ""
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Chyba pri ukladaní: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEB3B),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving
            ) {
                Text(
                    text = if (isSaving) "Ukladám..." else "Uložiť termín",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSaving) {
                CircularProgressIndicator(color = Color(0xFFFFEB3B))
            }
        }
    }
}
