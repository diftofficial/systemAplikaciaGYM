
// AuthViewModel.kt
package com.example.systemaplikacia100.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // Stav “role” – null znamená, že ešte nevieme rolu, po načítaní bude buď "user" alebo "admin"
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    /** Registrácia */
    fun register(name: String, email: String, password: String, phone: String, defaultRole: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                val credential = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = auth.currentUser
                    ?: throw Exception("Užívateľ sa nenašiel po registrácii.")

                // Uložíme displayName
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                // Uložíme zvyšné údaje do Firestore (vlastná kolekcia "users")
                val userData = mapOf(
                    "uid" to firebaseUser.uid,
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to "user", // pri registrácii vždy “user” – adminov budeme pridávať ručne v konzole Firestore
                    "points" to 0
                )
                db.collection("users")
                    .document(firebaseUser.uid)
                    .set(userData)
                    .await()

                _uiState.update { it.copy(isLoading = false, errorMessage = null, isSuccess = true) }
            } catch (e: Exception) {
                val errorMsg = when(e) {
                    is FirebaseAuthWeakPasswordException ->
                        "Heslo musí mať aspoň 6 znakov."
                    is FirebaseAuthInvalidCredentialsException ->
                        "Email má neplatný formát."
                    else ->
                        e.message ?: "Registrácia zlyhala."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
            }
        }
    }

    /** Prihlásenie */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.update { it.copy(isLoading = false, errorMessage = null, isSuccess = true) }
            } catch (e: Exception) {
                val errorMsg = when(e) {
                    is FirebaseAuthInvalidUserException ->
                        "Účet s týmto emailom neexistuje."
                    is FirebaseAuthInvalidCredentialsException ->
                        "Nesprávne prihlasovacie údaje."
                    else ->
                        e.message ?: "Prihlásenie zlyhalo."
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg, isSuccess = false) }
            }
        }
    }

    /** Načítanie roly (po úspešnom login‐e) */
    fun fetchUserRole() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                val doc = db.collection("users")
                    .document(uid)
                    .get()
                    .await()
                // Ak v dokumente "role" nenájdeme, predpokladáme "user"
                _userRole.value = doc.getString("role") ?: "user"
            } catch (_: Exception) {
                // ak zlyhá načítanie, môžeme defaultne súhlasiť “user”
                _userRole.value = "user"
            }
        }
    }

    /** Reset UI po navigácii/spätnom kroku */
    fun resetState() {
        _uiState.value = AuthUiState()
    }
}

