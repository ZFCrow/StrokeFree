package com.example.strokefree.ViewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.strokefree.classes.RecommendedExercise
import com.example.strokefree.classes.UserGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalCustomizationViewModel(
) : ViewModel() {

    var isEdit by mutableStateOf(false)
    var isRecommended by mutableStateOf(true)
    var showDialog by mutableStateOf(false)
    // Selected exercises mapped to number of sets (or reps)
    var selectedExercises = mutableStateMapOf<String, Int>()
    var userStrokeType= mutableStateOf("")

    init {
        fetchStrokeType()
    }

    private fun fetchStrokeType() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val userRef = FirebaseFirestore.getInstance().collection("users").document(uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val strokeType = document.getString("strokeType") ?: ""
                        userStrokeType.value = strokeType
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MyViewModel", "Error fetching strokeType", exception)
                }
        } else {
            Log.w("MyViewModel", "No current user found")
        }
    }
    // Toggle selection for an exercise
    fun toggleExerciseSelection(exercise: RecommendedExercise) {
        if (selectedExercises.containsKey(exercise.exerciseName)) {
            selectedExercises.remove(exercise.exerciseName) // Deselect if already selected
        } else {
            selectedExercises[exercise.exerciseName] = 1 // Default to 1 set if selected
        }
    }

    // Increment sets for an exercise
    fun incrementSets(exerciseName: String) {
        if (isExerciseSelected(exerciseName)) {
            selectedExercises[exerciseName] = (selectedExercises[exerciseName] ?: 0) + 1
        }
    }

    // Decrement sets for an exercise (minimum 1 set)
    fun decrementSets(exerciseName: String) {
        if (isExerciseSelected(exerciseName)) {
            val currentSets = selectedExercises[exerciseName] ?: 1
            if (currentSets > 1) {
                selectedExercises[exerciseName] = currentSets - 1
            } else {
                selectedExercises.remove(exerciseName) // Remove if decrementing from 1
            }
        }
    }

    // Clear all selections
    fun clearSelections() {
        selectedExercises.clear()
    }
    fun isExerciseSelected(exerciseName: String): Boolean {
        return selectedExercises.contains(exerciseName)
    }


}