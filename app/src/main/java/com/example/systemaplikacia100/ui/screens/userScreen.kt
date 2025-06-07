// src/main/java/com/example/systemaplikacia100/ui/screens/UserScreen.kt

package com.example.systemaplikacia100.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import com.example.systemaplikacia100.ui.screens.TrainingWidgetProvider


/**
 * Dátová trieda reprezentujúca jeden tréningový termín.
 * Pridané pole `trainerEmail` pre zobrazenie trénera.
 */
data class SessionItem(
    val sessionId: String,
    val trainerId: String,
    val trainerEmail: String,
    val trainerName: String, // nový parameter
    val dateTime: Timestamp,
    val capacity: Long,
    val priceInPoints: Long,
    val participantsCount: Long,
    val title: String = "",
    val description: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun userScreen(
    navController: NavController
) {
    // =======================================================
    // (A) NAČÍTANIE POUŽÍVATEĽA + BODY (points)
    // =======================================================
    val currentUser = Firebase.auth.currentUser
    var userPoints by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()
                val pts = snapshot.getLong("points") ?: 0L
                userPoints = pts
            } catch (e: Exception) {
                userPoints = 0L
            }
        } ?: run {
            userPoints = 0L
        }
    }

    // =======================================================
    // (B) NAČÍTANIE A ZOBRAZENIE BUDÚCICH SESSION
    //     (vrátane nahratia `trainerEmail` z kolekcie "users")
    // =======================================================
    var sessions by remember { mutableStateOf<List<SessionItem>>(emptyList()) }
    var isLoadingSessions by remember { mutableStateOf(true) }
    var sessionsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val nowTS = Timestamp.now()
            val querySnapshot = FirebaseFirestore.getInstance()
                .collection("sessions")
                .whereGreaterThanOrEqualTo("dateTime", nowTS)
                .orderBy("dateTime")
                .get()
                .await()

            // Pre každý dokument v "sessions" načítame aj e-mail trénera
            val list = querySnapshot.documents.mapNotNull { doc ->
                val ts = doc.getTimestamp("dateTime") ?: return@mapNotNull null
                val trainerId = doc.getString("trainerId") ?: return@mapNotNull null
                val title = doc.getString("title") ?: "(bez názvu)"
                val desc = doc.getString("description") ?: ""
                val capacity = doc.getLong("capacity") ?: 0L
                val price = doc.getLong("priceInPoints") ?: 0L
                val participants = doc.getLong("participantsCount") ?: 0L

                // Načítame e-mail trénera podľa trainerId
                val userDoc = try {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(trainerId)
                        .get()
                        .await()
                } catch (e: Exception) {
                    null
                }
                val trainerEmail = userDoc?.getString("email") ?: "(trener_nie_znamy)"
                val trainerName = userDoc?.getString("name") ?: "(trener_nie_znamy)"

                SessionItem(
                    sessionId = doc.id,
                    trainerId = trainerId,
                    trainerEmail = trainerEmail,  // uložíme e-mail
                    dateTime = ts,
                    capacity = capacity,
                    priceInPoints = price,
                    participantsCount = participants,
                    title = title,
                    description = desc,
                    trainerName = trainerName
                )
            }
            sessions = list
        } catch (e: Exception) {
            sessionsError = e.message
        } finally {
            isLoadingSessions = false
        }
    }

    // =======================================================
    // (C) STAV NA DETAIL TRÉNINGU (keď klikne používateľ)
    // =======================================================
    var selectedSession by remember { mutableStateOf<SessionItem?>(null) }
    var participantEmails by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingDetails by remember { mutableStateOf(false) }
    var detailsError by remember { mutableStateOf<String?>(null) }

    // =======================================================
    // (D) UI
    // =======================================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(48.dp))
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
                            popUpTo("userHome") { inclusive = true }
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

            Spacer(modifier = Modifier.height(48.dp))
            // (1) Privítanie
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser != null) {
                    Text(
                        text = "Vitaj, ${currentUser.displayName} \uD83D\uDD25",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Vitajte v aplikácii!",
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            // (2) Zobrazenie bodov
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "Body: ${userPoints ?: 0L}",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // (3) Zoznam tréningov
            if (isLoadingSessions) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFEB3B))
                }
            } else if (sessionsError != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chyba pri načítaní tréningov: $sessionsError",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(sessions) { session ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSession = session
                                    coroutineScope.launch {
                                        isLoadingDetails = true
                                        detailsError = null
                                        try {
                                            // Načítame prihlásených
                                            val partSnapshot = FirebaseFirestore.getInstance()
                                                .collection("sessionParticipants")
                                                .whereEqualTo("sessionId", session.sessionId)
                                                .get()
                                                .await()

                                            val emails = mutableListOf<String>()
                                            for (doc in partSnapshot.documents) {
                                                val userId = doc.getString("userId")
                                                if (!userId.isNullOrBlank()) {
                                                    val userDoc = FirebaseFirestore.getInstance()
                                                        .collection("users")
                                                        .document(userId)
                                                        .get()
                                                        .await()
                                                    val email = userDoc.getString("email")
                                                        ?: userId
                                                    emails.add(email)
                                                }
                                            }
                                            participantEmails = emails
                                        } catch (e: Exception) {
                                            detailsError = e.message
                                        } finally {
                                            isLoadingDetails = false
                                        }
                                    }
                                }
                        ) {
                            SessionCard(
                                session = session,
                                onJoinClicked = {
                                    coroutineScope.launch {
                                        // 1) Prihlásenie
                                        handleJoinSession(
                                            session,
                                            currentUser?.uid ?: "",
                                            context
                                        )


                                        // 2) Lokálne zníženie bodov
                                        //userPoints = (userPoints ?: 0L) - session.priceInPoints
                                        // 2) PO PRIHLÁSENÍ RE-LOAD BODY Z FIRESTORE
                                        //    (namiesto čisto lokálnej subtrakcie)
                                        try {
                                            currentUser?.uid?.let { uid ->
                                                val userSnapshot = FirebaseFirestore.getInstance()
                                                    .collection("users")
                                                    .document(uid)
                                                    .get()
                                                    .await()
                                                userPoints = userSnapshot.getLong("points") ?: 0L
                                            }
                                        } catch (_: Exception) {
                                            // V prípade chyby necháme userPoints, ako boli
                                        }
                                        // 3) Opätovný reload sessionov
                                        isLoadingSessions = true
                                        try {
                                            val nowTS = Timestamp.now()
                                            val newSnapshot = FirebaseFirestore.getInstance()
                                                .collection("sessions")
                                                .whereGreaterThanOrEqualTo("dateTime", nowTS)
                                                .orderBy("dateTime")
                                                .get()
                                                .await()
                                            sessions = newSnapshot.documents.mapNotNull { doc ->
                                                val ts = doc.getTimestamp("dateTime")
                                                    ?: return@mapNotNull null
                                                val trainerId = doc.getString("trainerId")
                                                    ?: return@mapNotNull null
                                                val title = doc.getString("title") ?: "(bez názvu)"
                                                val desc = doc.getString("description") ?: ""
                                                val capacity = doc.getLong("capacity") ?: 0L
                                                val price = doc.getLong("priceInPoints") ?: 0L
                                                val participants = doc.getLong("participantsCount")
                                                    ?: 0L

                                                // Znovu načítame e-mail trénera
                                                val userDoc = try {
                                                    FirebaseFirestore.getInstance()
                                                        .collection("users")
                                                        .document(trainerId)
                                                        .get()
                                                        .await()
                                                } catch (e: Exception) {
                                                    null
                                                }
                                                val trainerEmail = userDoc?.getString("email")
                                                    ?: "(trener_nie_znamy)"
                                                val trainerName = userDoc?.getString("name")
                                                    ?: "(trener_nie_znamy)"

                                                SessionItem(
                                                    sessionId = doc.id,
                                                    trainerId = trainerId,
                                                    trainerEmail = trainerEmail,
                                                    dateTime = ts,
                                                    capacity = capacity,
                                                    priceInPoints = price,
                                                    participantsCount = participants,
                                                    title = title,
                                                    description = desc,
                                                    trainerName = trainerName
                                                )
                                            }
                                        } catch (_: Exception) {
                                        } finally {
                                            isLoadingSessions = false
                                        }
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // (4) AlertDialog s detailmi tréningu
        if (selectedSession != null) {
            AlertDialog(
                onDismissRequest = {
                    selectedSession = null
                    participantEmails = emptyList()
                    detailsError = null
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedSession = null
                            participantEmails = emptyList()
                            detailsError = null
                        }
                    ) {
                        Text("Zavrieť")
                    }
                },
                title = {
                    Text(
                        text = selectedSession!!.title,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Dátum + čas + deň v týždni
                        val date = selectedSession!!.dateTime.toDate()
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        val dateStr = sdf.format(date)
                        val localDateTime = date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                        val dayOfWeek = localDateTime.dayOfWeek
                            .getDisplayName(TextStyle.FULL, Locale("sk"))
                        Text(
                            text = "Dátum: $dateStr ($dayOfWeek)",
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Tréner
                        Text(
                            text = "Tréner: ${selectedSession!!.trainerName}",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Popis
                        val descText = selectedSession!!.description.ifBlank { "Žiadny popis" }
                        Text(
                            text = "Popis: $descText",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Kapacita a cena
                        Text(
                            text = "Kapacita: ${selectedSession!!.participantsCount} / ${selectedSession!!.capacity}",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cena: ${selectedSession!!.priceInPoints} bodov",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Prihlásení účastníci
                        Text(
                            text = "Prihlásení účastníci:",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        when {
                            isLoadingDetails -> {
                                CircularProgressIndicator(color = Color(0xFFFFEB3B))
                            }
                            detailsError != null -> {
                                Text(
                                    text = "Chyba: $detailsError",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                            participantEmails.isEmpty() -> {
                                Text(
                                    text = "Nikto sa zatiaľ neprihlásil.",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                            else -> {
                                participantEmails.forEach { email ->
                                    Text(
                                        text = "• $email",
                                        color = Color.LightGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = Color(0xFF1E1E1E),
                //contentColor = Color.White
            )
        }
    }
}

/**
 * Karta jedného tréningu:
 *  - Zobrazuje názov, dátum, deň v týždni, trénera (email), krátky náhľad popisu
 *  - Kapacitu, cenu a tlačidlo „Prihlásiť sa“ (alebo „Plná kapacita“).
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SessionCard(
    session: SessionItem,
    onJoinClicked: () -> Unit
) {
    // Formátovanie dátumu + čas
    val date = session.dateTime.toDate()
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val dateStr = sdf.format(date)

    // Deň v týždni (po slovensky)
    val localDateTime = date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val dayOfWeek = localDateTime.dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale("sk"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
       // shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Názov tréningu
            Text(
                text = session.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Dátum + čas + deň
            Text(
                text = "$dateStr ($dayOfWeek)",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Tréner
            Text(
                text = "Tréner: ${session.trainerName}",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Krátky náhľad popisu (prvých 40 znakov)
            if (session.description.isNotBlank()) {
                val preview = if (session.description.length > 40)
                    session.description.substring(0, 40) + "…"
                else session.description
                Text(
                    text = "popis:  " + preview,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Kapacita a cena
            Text(
                text = "Kapacita: ${session.participantsCount} / ${session.capacity}",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cena: ${session.priceInPoints} bodov",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Tlačidlo alebo “Plná kapacita”
            if (session.participantsCount < session.capacity) {
                Button(
                    onClick = onJoinClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B),
                        contentColor = Color.Black
                    ),
                   // shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(text = "Prihlásiť sa", fontSize = 14.sp)
                }
            } else {
                Text(
                    text = "Plná kapacita",
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Prihlásenie používateľa na session:
 * 1) Overenie bodov používateľa.
 * 2) Overenie kapacity.
 * 3) Overenie, či už nie je prihlásený.
 * 4) (Batch) Zápis do sessionParticipants, zníženie bodov, zvýšenie participantsCount.
 */
suspend fun handleJoinSession(
    session: SessionItem,
    userId: String,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()

    // (1) Načítanie bodov používateľa
    val userDocRef = db.collection("users").document(userId)
    val userSnapshot = userDocRef.get().await()
    val currentPoints = userSnapshot.getLong("points") ?: 0L

    if (currentPoints < session.priceInPoints) {
        Toast.makeText(context, "Nemáte dostatok bodov.", Toast.LENGTH_SHORT).show()
        return
    }

    // (2) Overenie kapacity
    val sessionDocRef = db.collection("sessions").document(session.sessionId)
    val sessionSnapshot = sessionDocRef.get().await()
    val participantsCount = sessionSnapshot.getLong("participantsCount") ?: 0L
    val capacity = sessionSnapshot.getLong("capacity") ?: 0L

    if (participantsCount >= capacity) {
        Toast.makeText(context, "Kapacita bola práve vyčerpaná.", Toast.LENGTH_SHORT).show()
        return
    }

    // (3) Overenie, či užívateľ nie je prihlásený
    val participantDocRef = db.collection("sessionParticipants")
        .document("${session.sessionId}_$userId")
    val existing = participantDocRef.get().await()
    if (existing.exists()) {
        Toast.makeText(context, "Už ste prihlásený na tento termín.", Toast.LENGTH_SHORT).show()
        return
    }

    // (4) Batch: prihlásenie, zníženie bodov, update participantsCount
    val batch = db.batch()
    val participantData = mapOf(
        "sessionId" to session.sessionId,
        "userId" to userId,
        "timestampJoined" to Timestamp.now()
    )
    batch.set(participantDocRef, participantData)
    batch.update(userDocRef, "points", currentPoints - session.priceInPoints)
    batch.update(sessionDocRef, "participantsCount", participantsCount + 1)
    batch.commit().await()

    Toast.makeText(context, "Prihlásenie prebehlo úspešne!", Toast.LENGTH_SHORT).show()

    val awm = AppWidgetManager.getInstance(context)
    val ids = awm.getAppWidgetIds(ComponentName(context, TrainingWidgetProvider::class.java))
    val updateIntent = Intent(context, TrainingWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    }
    context.sendBroadcast(updateIntent)

}
