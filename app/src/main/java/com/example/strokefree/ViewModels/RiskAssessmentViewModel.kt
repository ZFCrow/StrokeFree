package com.example.strokefree.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Updated data class – renamed userName to userId for clarity
data class RiskAssessment(
    val assessmentId: String,
    val userId: String,
    val riskResult: String,
    val timestamp: Long,
    val bmi: Double,
    val avgGlucoseLevel: Double
)

class RiskAssessmentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _pastAssessments = MutableStateFlow<List<RiskAssessment>>(emptyList())
    val pastAssessments: StateFlow<List<RiskAssessment>> = _pastAssessments

    fun fetchPastAssessments() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("RiskAssessmentViewModel", "User not authenticated")
            return
        }
        // Directly use the current user's UID for querying
        fetchRiskAssessments(currentUser.uid)
    }

    private fun fetchRiskAssessments(userId: String) {
        Log.d("RiskAssessmentViewModel", "Fetching assessments for user ID: $userId")

        viewModelScope.launch {
            db.collection("riskAssessments")
                .whereEqualTo("userId", userId)  // Updated query filter to userId
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("RiskAssessmentViewModel", "No assessments found for user ID: $userId")
                    } else {
                        Log.d("RiskAssessmentViewModel", "Fetched ${documents.size()} assessments for user ID: $userId")
                    }

                    val assessments = documents.map { doc ->
                        RiskAssessment(
                            assessmentId = doc.getString("assessmentId") ?: "Unknown",
                            userId = doc.getString("userId") ?: "Unknown",
                            riskResult = doc.getString("riskResult") ?: "Unknown",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            bmi = doc.getDouble("bmi") ?: 0.0,
                            avgGlucoseLevel = doc.getDouble("avgGlucoseLevel") ?: 0.0
                        )
                    }
                    _pastAssessments.value = assessments
                }
                .addOnFailureListener { e ->
                    Log.e("RiskAssessmentViewModel", "Error fetching past assessments", e)
                }
        }
    }

    fun fetchAssessmentById(assessmentId: String, callback: (Map<String, Any>?, String?) -> Unit) {
        db.collection("riskAssessments").document(assessmentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    val assessmentDetails = data.toMutableMap()
                    assessmentDetails["residenceType"] = document.getString("residenceType") ?: "Unknown"
                    assessmentDetails["bmi"] = document.getDouble("bmi") ?: 0.0
                    assessmentDetails["avgGlucoseLevel"] = document.getDouble("avgGlucoseLevel") ?: 0.0
                    callback(assessmentDetails, null)
                } else {
                    callback(null, "No assessment found for this ID")
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.message)
            }
    }
}
