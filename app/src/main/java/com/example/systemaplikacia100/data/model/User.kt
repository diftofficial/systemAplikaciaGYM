package com.example.systemaplikacia100.data.model


data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ""
) {
    // Bezparametrický konštruktor pre Firestore deserializáciu
    constructor() : this("", "", "", "", "")
}
