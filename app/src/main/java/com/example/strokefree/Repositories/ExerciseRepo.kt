package com.example.strokefree.Repositories

import android.util.Log
import com.example.strokefree.classes.RecommendedExercise
import com.google.firebase.firestore.FirebaseFirestore

object ExerciseRepo {
    fun retrieveRecommendedExercises(
        onSuccess: (List<RecommendedExercise>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("exercises")
            .get()
            .addOnSuccessListener { result ->
                val recommendedExercises = mutableListOf<RecommendedExercise>()
                for (document in result) {
                    val exercise = document.toObject(RecommendedExercise::class.java)
                    recommendedExercises.add(exercise)
                }
                onSuccess(recommendedExercises)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }


}