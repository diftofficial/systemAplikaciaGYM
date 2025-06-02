package com.example.systemaplikacia100.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.systemaplikacia100.viewmodel.AuthViewModel
import com.example.systemaplikacia100.viewmodel.AuthUiState

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    // Stavové premenné pre vstupné polia
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    // Rolu už neukladáme z UI, default v kóde bude “user”
    val defaultRole = "user"

    val uiState: AuthUiState by viewModel.uiState.collectAsState()

    // Po úspešnej registrácii navigovať na hlavnú obrazovku
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate("userHome") {
                popUpTo("login") { inclusive = true }
            }
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
        ) {
            Text(text = "Registrácia", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Meno") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Heslo") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefón (voliteľné)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Pri registrácii posielame defaultRole = "user"
                    viewModel.register(
                        name.trim(),
                        email.trim(),
                        password,
                        phone.trim(),
                        defaultRole
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                // Telefón je už voliteľné, takže ho nepotrebujeme overovať v enabled.
                enabled = !uiState.isLoading && name.isNotBlank()
                        && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Vytvoriť účet")
            }

            TextButton(
                onClick = {
                    viewModel.resetState()
                    navController.popBackStack()  // Vrátiť sa späť na LoginScreen
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Máte už účet? Prihláste sa")
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
