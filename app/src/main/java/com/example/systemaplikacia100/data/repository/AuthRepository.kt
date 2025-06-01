package com.example.systemaplikacia100.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.example.systemaplikacia100.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    // Získanie inštancií FirebaseAuth a FirebaseFirestore pomocou KTX
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    /**
     * Registrácia nového používateľa (vytvorenie účtu v Auth a uloženie údajov do Firestore).
     * @throws Exception ak registrácia zlyhá (obsahuje chybovú správu).
     */
    suspend fun registerUser(name: String, email: String, password: String, phone: String, role: String) {
        // Vytvorenie používateľa v Firebase Authentication
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val newUser: FirebaseUser? = result.user  // novo registrovaný Firebase používateľ
        if (newUser != null) {
            // Pripravíme objekt User s údajmi pre Firestore
            val userId = newUser.uid
            val user = User(id = userId, name = name, email = email, phone = phone, role = role)
            // Uloženie objektu do Firestore (kolekcia "users")
            firestore.collection("users").document(userId).set(user).await()
        }
        // Ak je newUser null, pravdepodobne registrácia zlyhala bez vyhodenej výnimky
    }

    /**
     * Prihlásenie existujúceho používateľa pomocou emailu a hesla.
     * @throws Exception ak prihlásenie zlyhá (obsahuje chybovú správu).
     */
    suspend fun loginUser(email: String, password: String) {
        // Pokus o prihlásenie používateľa
        auth.signInWithEmailAndPassword(email, password).await()
        // Po úspechu (ak nenastala výnimka) bude aktuálny používateľ dostupný cez auth.currentUser
    }

    /** Odhlásenie aktuálne prihláseného používateľa. */
    fun logout() {
        auth.signOut()
    }
}
