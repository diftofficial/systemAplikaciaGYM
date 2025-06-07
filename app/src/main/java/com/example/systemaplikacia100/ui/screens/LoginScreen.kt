package com.example.systemaplikacia100.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.systemaplikacia100.R
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.example.systemaplikacia100.viewmodel.AuthUiState

import androidx.compose.ui.text.TextStyle

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val uiState: AuthUiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Po úspešnom login-e presmerujeme na "splash"
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("splash") {
                // odstránime login zo spätného zásobníka
                popUpTo("login") { inclusive = true }
            }
            viewModel.resetState()
        }
        val awm = AppWidgetManager.getInstance(context)
        val ids = awm.getAppWidgetIds(ComponentName(context, TrainingWidgetProvider::class.java))
        val updateIntent = Intent(context, TrainingWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(updateIntent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // celé pozadie čierne
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nadpis: "Prihlás sa do svojho účtu"
            Text(
                text = "Prihlás sa",
                color = Color(0xFFFFEB3B),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "do svojho účtu",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --------------------------------------------------
            // TextField pre meno / tel.číslo (bez outlinedTextFieldColors)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "email",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                textStyle = TextStyle(color = Color.White),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FiftySixDp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // --------------------------------------------------
            // TextField pre heslo (bez outlinedTextFieldColors)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "heslo",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                textStyle = TextStyle(color = Color.White),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FiftySixDp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Žlté tlačidlo "prihlásiť sa"
            Button(
                onClick = {
                    viewModel.login(email.trim(), password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FiftySixDp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEB3B), // jemná žltá
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                Text(
                    text = "prihlásiť sa",
                    fontSize = sixteenSp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Text "ak nemáš účet Registruj sa"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ak nemáš účet",
                    color = Color.LightGray,
                    fontSize = fourteenSp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Registruj sa",
                    color = Color(0xFFFFEB3B),
                    fontSize = fourteenSp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Tlačidlo "registruj sa"
            OutlinedButton(
                onClick = {
                    viewModel.resetState()
                    navController.navigate("register") {
                        popUpTo("login") { inclusive = false }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FiftySixDp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    //color = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "registruj sa",
                    fontSize = sixteenSp
                )
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFFFFEB3B)
                )
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = fourteenSp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Logo v pravom dolnom rohu
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

// Preddefinované konštanty pre rozmery
private val FiftySixDp = 56.dp
private val sixteenSp = 16.sp
private val fourteenSp = 14.sp
