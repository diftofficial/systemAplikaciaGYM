package com.example.systemaplikacia100.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /** Vráti sa z tohto suspendu až keď Firebase celá operácia prebehne (úspech alebo výnimka). */
    @Throws(Exception::class)
    suspend fun registerUser(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: String
    ) {
        // 1) Zaregistruj e-mail/heslo
        val credential = auth.createUserWithEmailAndPassword(email, password)
            .await()  // <-- ČÍTAJ: .await() predĺži coroutine dovtedy, kým Firebase autentifikácia neskončí

        // 2) Ak chceš do profilovej časti uložiť displayName:
        val firebaseUser = auth.currentUser ?: throw Exception("Užívateľ sa nenašiel po registrácii.")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        firebaseUser.updateProfile(profileUpdates)
            .await()

        // 3) Ulož ďalšie údaje (telefón, rolu) do Firestore (alebo vlastnej db kolekcie „users“)
        val userData = mapOf(
            "uid" to firebaseUser.uid,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "role" to role
        )
        db.collection("users")
            .document(firebaseUser.uid)
            .set(userData)
            //.await()
        // Ak všetko prebehlo v poriadku, táto metadata skončí úspešne. Ak niečo zlyhá,
        // .await() vyhodí výnimku, ktorú zachytíš v AuthViewModel.
    }

    /** Podobne pre login: */
    @Throws(Exception::class)
    suspend fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .await() // toto .await() zabezpečí, že coroutine čaká na výsledok Firebase
    }
}


