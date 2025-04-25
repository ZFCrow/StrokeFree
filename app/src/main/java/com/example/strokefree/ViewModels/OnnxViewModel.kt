package com.example.strokefree.ViewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.strokefree.OnnxModelHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnnxViewModel(application: Application) : AndroidViewModel(application) {
    private val modelHelper = OnnxModelHelper(application as Context)
    private val db = FirebaseFirestore.getInstance()
    private val _result = MutableStateFlow<Int?>(null)
    val result: StateFlow<Int?> = _result

    init {
        modelHelper.loadModel("model.onnx")
    }

    private fun saveRiskAssessment(userId: String, assessmentId: String, details: Map<String, Any>) {
        db.collection("riskAssessments").document(assessmentId)
            .set(details)
            .addOnSuccessListener {
                Log.d("OnnxViewModel", "Risk assessment saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("OnnxViewModel", "Error saving risk assessment", e)
            }
    }

    fun resetResult() {
        _result.value = null // Reset result so it doesn’t auto-navigate
    }

    fun run(
        userId: String,  // Updated parameter: using userId
        age: Float,
        gender: String,
        hypertension: String?,
        heartDisease: String?,
        everMarried: String?,
        avgGlucoseLevel: String,
        bmi: String,
        workType: String?,
        smokingStatus: String?,
        residenceType: String?
    ) {
        Log.d("OnnxViewModel", "Running Inference...")

        // Encode gender
        val genderMale = if (gender == "Male") 1f else 0f
        val genderOther = if (gender != "Male") 1f else 0f

        val hypertensionValue = if (hypertension == "Yes") 1f else 0f
        val heartDiseaseValue = if (heartDisease == "Yes") 1f else 0f
        val everMarriedValue = if (everMarried == "Yes") 1f else 0f
        val avgGlucoseLevelValue = avgGlucoseLevel.toFloatOrNull() ?: 0f
        val bmiValue = bmi.toFloatOrNull() ?: 0f

        // Encode work type
        val workTypeNeverWorked = if (workType == "Never worked") 1f else 0f
        val workTypePrivate = if (workType == "Private/Self-employed") 1f else 0f
        val workTypeSelfEmployed = if (workType == "Govt_job") 1f else 0f
        val workTypeChildren = 0f

        // Encode smoking status
        val smokingFormerly = if (smokingStatus == "Formerly smoked") 1f else 0f
        val smokingNever = if (smokingStatus == "Never smoked") 1f else 0f
        val smokingSmokes = if (smokingStatus == "Smokes") 1f else 0f

        val residenceUrban = if (residenceType == "HDB") 1f else 0f

        val inputData = arrayOf(
            floatArrayOf(
                age, hypertensionValue, heartDiseaseValue, everMarriedValue,
                avgGlucoseLevelValue, bmiValue, genderMale, genderOther,
                workTypeNeverWorked, workTypePrivate, workTypeSelfEmployed, workTypeChildren,
                residenceUrban, smokingFormerly, smokingNever, smokingSmokes
            )
        )

        Log.d("OnnxViewModel", "Input Data: ${inputData.contentDeepToString()}")

        val assessmentId = "RA_${System.currentTimeMillis()}" // Unique assessment ID

        viewModelScope.launch {
            val output = modelHelper.runInference(inputData)
            val isHighRisk = output == 1
            Log.d("OnnxViewModel", "Model Output: $output")
            _result.value = output

            val assessmentDetails = mapOf<String, Any>(
                "userId" to (userId ?: "UnknownUser"), // Updated key to userId
                "assessmentId" to assessmentId,
                "age" to age.toDouble(),  // Ensure numerical values are Double
                "gender" to (gender ?: "Unknown"),
                "hypertension" to (hypertension ?: "Unknown"),
                "heartDisease" to (heartDisease ?: "Unknown"),
                "everMarried" to (everMarried ?: "Unknown"),
                "avgGlucoseLevel" to (avgGlucoseLevel.toDoubleOrNull() ?: 0.0),
                "bmi" to (bmi.toDoubleOrNull() ?: 0.0),
                "workType" to (workType ?: "Unknown"),
                "smokingStatus" to (smokingStatus ?: "Unknown"),
                "residenceType" to (residenceType ?: "Unknown"),
                "riskResult" to (if (isHighRisk) "High Risk" else "Low Risk"),
                "timestamp" to System.currentTimeMillis()
            )

            saveRiskAssessment(userId, assessmentId, assessmentDetails)
        }
    }
}
