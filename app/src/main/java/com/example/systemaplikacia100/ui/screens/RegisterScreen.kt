// src/main/java/com/example/systemaplikacia100/ui/screens/RegisterScreen.kt

package com.example.systemaplikacia100.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.systemaplikacia100.R
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.example.systemaplikacia100.viewmodel.AuthUiState

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // -------------------------------------------------------
    // 1) Stavové premenné pre vstupné polia
    // -------------------------------------------------------
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    val defaultRole = "user"

    val uiState: AuthUiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // -------------------------------------------------------
    // 2) Po úspešnej registrácii navigujeme späť na hlavný
    // -------------------------------------------------------
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("userHome") {
                popUpTo("login") { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    // -------------------------------------------------------
    // 3) Hlavný layout
    // -------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // tmavé pozadie
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------------------------------------------------
            // 3.a) Nadpis „Registrácia“
            // ---------------------------------------------------
            Text(
                text = "Registrácia",
                fontSize = 40.sp,
                color = Color(0xFFFFEB3B), // žltá
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // ---------------------------------------------------
            // 3.b) Vstup: Meno
            // ---------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (name.isEmpty()) {
                    Text(
                        text = "Meno",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ---------------------------------------------------
            // 3.c) Vstup: Email
            // ---------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (email.isEmpty()) {
                    Text(
                        text = "Email",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ---------------------------------------------------
            // 3.d) Vstup: Heslo
            // ---------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (password.isEmpty()) {
                    Text(
                        text = "Heslo",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ---------------------------------------------------
            // 3.e) Vstup: Telefón (voliteľné)
            // ---------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (phone.isEmpty()) {
                    Text(
                        text = "Telefón (voliteľné)",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // ---------------------------------------------------
            // 3.f) Tlačidlo „Vytvoriť účet“
            // ---------------------------------------------------
            Button(
                onClick = {
                    viewModel.register(
                        name.trim(),
                        email.trim(),
                        password,
                        phone.trim(),
                        defaultRole
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEB3B),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
                        && name.isNotBlank()
                        && email.isNotBlank()
                        && password.isNotBlank()
            ) {
                Text(
                    text = "vytvoriť profil",
                    fontSize = 16.sp
                )
            }

            // ---------------------------------------------------
            // 3.g) Loading indikátor + Chybové správy
            // ---------------------------------------------------
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color(0xFFFFEB3B)
                )
            }
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // ---------------------------------------------------
            // 3.h) Prechod späť na Login
            // ---------------------------------------------------
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    viewModel.resetState()
                    navController.popBackStack()
                }
            ) {
                Text(
                    text = "Máte už účet? Prihláste sa",
                    color = Color.White
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
