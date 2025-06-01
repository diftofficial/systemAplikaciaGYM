package com.example.systemaplikacia100.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.example.systemaplikacia100.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI stav pre autentifikačné operácie */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val authRepo = AuthRepository()

    // StateFlow pre stav UI, defaultne žiadna chyba a nič sa nedeje
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    /**
     * Registrácia nového používateľa cez AuthRepository.
     *
     * @param name     Meno používateľa
     * @param email    E-mail adresa
     * @param password Heslo
     * @param phone    Telefónne číslo
     * @param role     Rola používateľa (napr. "Client", "Administrator" …)
     */
    fun register(name: String, email: String, password: String, phone: String, role: String) {
        viewModelScope.launch {
            // nastavíme loading stav
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }

            try {
                // Tu sa reálne volá metóda repository, ktorá interné pracuje s FirebaseAuth
                authRepo.registerUser(name, email, password, phone, role)

                // Ak nič nevhodné nevyhodilo výnimku, považujeme registráciu za úspešnú
                _uiState.update { it.copy(isLoading = false, errorMessage = null, isSuccess = true) }
                Log.i("AuthViewModel", "Registrácia úspešná pre email=$email")

            } catch (e: Exception) {
                // Vykonáme logovanie celej výnimky, aby sme pri debugovaní videli stacktrace
                Log.e("AuthViewModel", "Registrácia zlyhala pre email=$email", e)

                // Rozpoznáme niektoré špecifické Firebase Auth výnimky
                val errorMsg = when (e) {
                    is FirebaseAuthWeakPasswordException ->
                        "Heslo musí mať aspoň 6 znakov."
                    is FirebaseAuthInvalidCredentialsException ->
                        "Email má neplatný formát."
                    is FirebaseAuthUserCollisionException ->
                        "Používateľ s týmto emailom už existuje."
                    is FirebaseAuthInvalidUserException ->
                        "Účet s týmto emailom neexistuje." // teoreticky menej pravdepodobné pri registrácii
                    is FirebaseFirestoreException ->
                        // ak niekde v repo vytváraš dokumenty v Firestore a vyhodí sa FirestoreException
                        "Chyba pri prístupe k databáze. Skúste to neskôr."
                    else -> {
                        // Ak je to sieťová chyba (napr. timeout, offline) alebo iná generická chyba
                        e.message ?: "Registrácia zlyhala z neznámej príčiny."
                    }
                }

                // Nastavíme stav UI na error
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
            }
        }
    }

    /**
     * Prihlásenie používateľa cez AuthRepository.
     *
     * @param email    E-mail adresa
     * @param password Heslo
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                authRepo.loginUser(email, password)

                _uiState.update { it.copy(isLoading = false, errorMessage = null, isSuccess = true) }
                Log.i("AuthViewModel", "Prihlásenie úspešné pre email=$email")

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Prihlásenie zlyhalo pre email=$email", e)

                val errorMsg = when (e) {
                    is FirebaseAuthInvalidUserException ->
                        "Účet s týmto emailom neexistuje."
                    is FirebaseAuthInvalidCredentialsException ->
                        "Nesprávne prihlasovacie údaje."
                    is FirebaseAuthUserCollisionException ->
                        "Používateľ s týmto emailom už existuje." // menej bežné pri login
                    is FirebaseAuthWeakPasswordException ->
                        "Heslo je príliš slabé." // menej bežné pri login
                    is FirebaseFirestoreException ->
                        "Chyba pri prístupe k databáze."
                    else ->
                        e.message ?: "Prihlásenie zlyhalo z neznámej príčiny."
                }

                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
            }
        }
    }

    /**
     * Vynuluje stav UI (napr. po úspešnej akcii alebo pri odchode z obrazovky).
     */
    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
