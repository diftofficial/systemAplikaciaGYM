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
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    // Stavové premenné pre vstupné polia
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Odoberáme stav z AuthViewModel-u (Compose kompatibilný StateFlow -> State)
    val uiState: AuthUiState by viewModel.uiState.collectAsState()

    // Ak je používateľ úspešne prihlásený, navigujeme na hlavnú obrazovku
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Vyčistenie backstacku: odstránenie LoginScreen (prípadne celej grafu auth) z histórie
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }   // odstráni "login" z backstacku:contentReference[oaicite:12]{index=12}
            }
            viewModel.resetState()  // vynulujeme stav, aby sa neuložil chybový status do ďalšej obrazovky
        }
    }

    // UI rozloženie obrazovky
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier
            .fillMaxWidth(0.8f)  // stĺpec bude zaberať 80% šírky obrazovky
        ) {
            Text(text = "Prihlásenie", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(32.dp))
            // TextField pre Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            // TextField pre Heslo
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Heslo") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),  // skrytie hesla
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Tlačidlo "Prihlásiť sa"
            Button(
                onClick = { viewModel.login(email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Prihlásiť sa")
            }
            // Odkaz na registračnú obrazovku
            TextButton(
                onClick = {
                    viewModel.resetState()  // vyčisti prípadné staré chyby pred prechodom
                    navController.navigate("register")
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Nemáte účet? Zaregistrujte sa")
            }

            // Zobrazenie priebehu alebo chyby, ak existujú
            if (uiState.isLoading) {
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
