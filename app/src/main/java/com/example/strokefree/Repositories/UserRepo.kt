package com.example.strokefree.Repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object UserRepo {
    fun createUserDocument(
        userID: String,
        name: String? = null,
        email: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Build a map with the user data.
        val userData = mutableMapOf<String, Any>()
        name?.let { userData["name"] = it }
        email?.let { userData["email"] = it }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userID)
            .set(userData)
            .addOnSuccessListener {
                Log.d("UserRepo", "User document created successfully, proceeding to onSuccess()")
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}