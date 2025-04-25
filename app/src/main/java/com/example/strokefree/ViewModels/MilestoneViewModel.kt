package com.example.strokefree.ViewModels

import android.util.Log
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class MilestoneViewModel : ViewModel(){
    //automatically generate more when user scrolls to the end
    var milestoneGoal=mutableStateOf(50)

    val daysCompleted = mutableStateOf<Int>(0)
    val longestStreak = mutableStateOf<Int>(0)
    var lastCompletedDate= mutableStateOf<LocalDate?>(null)

    val multiple = mutableStateOf<Int>(7)
    val nextComingMilestone by derivedStateOf {
        val completed = daysCompleted.value
        val multipleOf = multiple.value

        multipleOf - (completed % multipleOf)
    }
    var userId by mutableStateOf("")
    private var streakListener: ListenerRegistration? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Update userId and load data
            userId = currentUser.uid
            listenToStreakData()
//            updateNextMilestone()

        }
    }

    init {
        // Add the auth state listener so that it triggers once a user logs in.
        firebaseAuth.addAuthStateListener(authListener)
    }

    fun listenToStreakData() {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(userId)

        // Remove the old listener if it exists (avoid memory leaks)
        streakListener?.remove()

        // Attach a new snapshot listener
        streakListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("MilestoneVM", "Error listening to streak data", error)
                return@addSnapshotListener
            }

            // If the document doesn't exist, initialize streak
            if (snapshot == null || !snapshot.exists()) {
                initializeStreakInFirestore(userRef)
                return@addSnapshotListener
            }

            // Parse streak data from the doc
            val streakMap = snapshot.get("streak") as? Map<String, Any>
            if (streakMap == null) {
                initializeStreakInFirestore(userRef)
                return@addSnapshotListener
            }

            val days = (streakMap["daysCompleted"] as? Number)?.toInt() ?: 0
            val longest = (streakMap["longestStreak"] as? Number)?.toInt() ?: 0
            val lastDateString = streakMap["lastCompletedDate"] as? String ?: ""

            val lastDate = parseDate(lastDateString)
            val zoneId = ZoneId.of("Asia/Singapore")
            val yesterday = LocalDate.now(zoneId).minusDays(1)
            val today = LocalDate.now(zoneId)

            // Check if lastDate is consecutive (yesterday or today)
            val isConsecutive = (lastDate == yesterday) ||  (lastDate == today)

            if (isConsecutive) {
                daysCompleted.value = days
                longestStreak.value = longest
                lastCompletedDate.value = lastDate
                Log.d("MilestoneVM", "Streak is consecutive. daysCompleted=$days")
            } else {
                // If not consecutive, reset in Firestore
                resetStreakInFirestore(userRef, longest)
            }
//            updateNextMilestone()

        }
    }
//    fun updateNextMilestone() {
//        val completed = daysCompleted.value
//        val multipleOf = multiple.value
//
//        // Find the next nearest multiple of `multiple.value` after `daysCompleted`
//        val nextMilestone = if (completed % multipleOf == 0) {
//            completed + multipleOf // If already a multiple, move to next multiple
//        } else {
//            ((completed / multipleOf) + 1) * multipleOf
//        }
//
//        nextComingMilestone.value = nextMilestone
//    }

    private fun initializeStreakInFirestore(userRef: DocumentReference) {
        val defaultStreak = mapOf(
            "daysCompleted" to 0,
            "longestStreak" to 0,
            "lastCompletedDate" to ""
        )
        userRef.update("streak", defaultStreak)
            .addOnSuccessListener {
                daysCompleted.value = 0
                longestStreak.value = 0
                lastCompletedDate.value = null
                Log.d("Firestore", "Streak field initialized to defaults.")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error setting default streak")
            }
    }

    private fun resetStreakInFirestore(userRef: DocumentReference, longest: Int) {
        daysCompleted.value = 0
        longestStreak.value = longest
        lastCompletedDate.value = null

        val newStreak = mapOf(
            "daysCompleted" to 0,
            "longestStreak" to longest,
            "lastCompletedDate" to ""
        )
        userRef.update("streak", newStreak)
            .addOnSuccessListener {
                Log.d("Firestore", "Streak reset (non-consecutive).")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error resetting streak")
            }
    }
    private fun parseDate(dateString: String): LocalDate? {
        return if (dateString.isNotEmpty()) {
            LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } else {
            null
        }
    }
    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}

