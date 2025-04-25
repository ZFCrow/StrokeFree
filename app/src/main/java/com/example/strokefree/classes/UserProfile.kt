package com.example.strokefree.classes

data class UserProfile (
    val userID: String = "",
    val name: String = "",
    val dob: String = "",
    val gender: String = "",
    val bloodType: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val medicalConditions: List<String> = emptyList(),
    val strokeType: String = "",
    val imageURL: String = ""
)
