package com.example.strokefree.ViewModels

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.strokefree.classes.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.util.Base64
import com.example.strokefree.classes.RiskFactor

class ProfileViewModel: ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = mutableStateOf<UserProfile?>(null)
    val userProfile: State<UserProfile?> = _userProfile

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    private val _userID = mutableStateOf<String?>(null)
    val userID: State<String?> = _userID

    private val allRiskFactors = listOf(
        RiskFactor(
            condition = "Diabetes",
            severity = "High",
            impactDescription = "High blood sugar can damage blood vessels, increasing stroke risk."
        ),
        RiskFactor(
            condition = "Hypertension",
            severity = "Very High",
            impactDescription = "High blood pressure is the top cause of stroke due to arterial damage."
        ),
        RiskFactor(
            condition = "Heart Disease",
            severity = "High",
            impactDescription = "Atrial fibrillation and other conditions increase clot-related stroke risk."
        ),
        RiskFactor(
            condition = "Cancer",
            severity = "Moderate",
            impactDescription = "Certain cancers and treatments can increase blood clotting and stroke likelihood."
        ),
        RiskFactor(
            condition = "Asthma",
            severity = "Low to Moderate",
            impactDescription = "Chronic inflammation and reduced oxygen may slightly raise stroke risk."
        ),
        RiskFactor(
            condition = "Others",
            severity = "Variable",
            impactDescription = "Unspecified conditions may increase stroke risk via inflammation, inactivity, or medication."
        )
    )

//    init {
//        fetchCurrentUser()
//    }

    fun fetchCurrentUser() {
        val currentUser = auth.currentUser
        if(currentUser != null) {
            _userID.value = currentUser.uid
            fetchUserProfile(currentUser.uid)
        } else {
            _error.value = "User not authenticated"
        }
    }

    fun fetchUserProfile(userID:String) {
        _loading.value = true

        db.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _userProfile.value = UserProfile(
                        userID = document.id,
                        name = document.getString("name") ?: "",
                        dob = document.getString("DOB") ?: "",
                        gender = document.getString("gender") ?: "Male",
                        email = document.getString("email") ?: "",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        medicalConditions = (document.get("medicalConditions") as? List<*>)
                            ?.filterIsInstance<String>() ?: emptyList(),
                        bloodType = document.getString("bloodType") ?: "",
                        strokeType = document.get("strokeType") as? String ?: "",
                        imageURL = document.getString("imageURL") ?: ""
                    )
                } else {
                    _error.value = "User Details not found"
                }

                _loading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message.toString()
                _loading.value = false
            }
    }

    fun updateUserProfile(userID: String, name:String, dob:String, gender: String, email: String, phoneNumber: String) {
        val userId = auth.currentUser?.uid ?:return
        _loading.value = true

        val updatedData = mapOf(
            "id" to userID,
            "name" to name,
            "DOB" to dob,
            "gender" to gender,
            "email" to email,
            "phoneNumber" to phoneNumber
        )

        db.collection("users").document(userId).update(updatedData)
            .addOnSuccessListener {
                _userProfile.value = _userProfile.value?.copy(
                    name = name,
                    dob = dob,
                    email = email,
                    phoneNumber = phoneNumber
                )

                db.collection("posts")
                    .whereEqualTo("authorId", userId)
                    .get()
                    .addOnSuccessListener { postSnapshot ->
                        for (postDoc in postSnapshot.documents) {
                            db.collection("posts")
                                .document(postDoc.id)
                                .update("authorName", name)
                        }
                    }

                db.collection("posts")
                    .get()
                    .addOnSuccessListener { allPosts ->
                        for (postDoc in allPosts.documents) {
                            val postId = postDoc.id
                            db.collection("posts")
                                .document(postId)
                                .collection("comments")
                                .whereEqualTo("authorId", userId)
                                .get()
                                .addOnSuccessListener { commentsSnapshot ->
                                    for (commentDoc in commentsSnapshot.documents) {
                                        db.collection("posts")
                                            .document(postId)
                                            .collection("comments")
                                            .document(commentDoc.id)
                                            .update("authorName", name)
                                    }
                                }
                        }
                    }

                _loading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message.toString()
                _loading.value = false
            }

    }

    fun uploadProfileImageBase64(context: Context, userId: String, imageUri: Uri, onResult: (Boolean) -> Unit) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()

            if (bytes != null) {
                val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("imageURL", base64String)
                    .addOnSuccessListener {
                        fetchUserProfile(userId)
                        onResult(true)
                    }
                    .addOnFailureListener { e ->
                        _error.value = e.message.toString()
                        onResult(false)
                    }
            } else {
                onResult(false)
            }
        } catch (e: Exception) {
            _error.value = e.message.toString()
            onResult(false)
        }
    }

    fun getUserRiskFactors(): List<RiskFactor> {
        val conditions = _userProfile.value?.medicalConditions ?: return emptyList()
        return conditions.map { condition ->
            allRiskFactors.find { it.condition == condition }
                ?: RiskFactor(
                    condition = condition,
                    severity = "Unknown",
                    impactDescription = "Potential risk factor. Please consult your doctor for more info."
                )
        }
    }


    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        _userProfile.value = null
        onSuccess()
    }



}