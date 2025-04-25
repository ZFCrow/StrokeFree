package com.example.strokefree.ViewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.strokefree.Repositories.ExerciseRepo
import com.example.strokefree.classes.Exercise
import com.example.strokefree.classes.ExerciseLog
import com.example.strokefree.classes.RecommendedExercise
import com.example.strokefree.classes.UserGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.type.DateTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ExercisesViewModel : ViewModel(){

    val recommended = mutableStateListOf<RecommendedExercise>()
    var userId by mutableStateOf("") // Will be updated once a user is logged in
    private var userLogListener: ListenerRegistration? = null

    var exerciseLogs = mutableStateListOf<ExerciseLog>()

    val userGoals = mutableStateListOf<UserGoal>()

    // Flag to ensure data is loaded only once after authentication
    private var dataPopulated by mutableStateOf(false)

    private val firebaseAuth = FirebaseAuth.getInstance()

//    private val authListener = FirebaseAuth.AuthStateListener { auth ->
//        Log.d ("AuthStateListener", "Auth state changed")
//        val currentUser = auth.currentUser
//        if (currentUser != null && !dataPopulated) {
//            // Update userId and load data
//            userId = currentUser.uid
//            populateViewModelData()
//            dataPopulated = true
//        }
//        Log.d("DataPopulated", "Populated Data from Firebase of User $userId")
//    }
//
//    init {
//        // Add the auth state listener so that it triggers once a user logs in.
//        firebaseAuth.addAuthStateListener(authListener)
//    }
//
//    override fun onCleared() {
//        firebaseAuth.removeAuthStateListener(authListener)
//        super.onCleared()
//    }

    fun populateViewModelData() {
        retrieveRecommendedFromFirebase()
        this.userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        populateUserGoals()
        listenToUserExerciseLog()
    }


    fun populateUserGoals() {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                document.get("userGoals")?.let { goals ->
                    userGoals.clear()
                    userGoals.addAll((goals as List<Map<String, Any>>).map {
                        UserGoal(it["name"] as? String ?: "Unknown", (it["sets"] as? Long)?.toInt() ?: 0)
                    })
                    updateGoalsWithLogData()
                } ?: Log.d("Firestore", "No user goals found.")
            }
            .addOnFailureListener { Log.e("Firestore", "Error fetching user goals", it) }
    }


    // Moved Inside the ViewModel
    private fun retrieveRecommendedFromFirebase() {
        ExerciseRepo.retrieveRecommendedExercises(
            onSuccess = { ExerciseList ->
                recommended.clear()
                recommended.addAll(ExerciseList)
                Log.d("Recommended", "Fetched ${recommended.size} exercises.")
            },
            onFailure = {}
        )
    }

    fun updateUserGoals(newGoals: List<UserGoal>) {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(userId)
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Singapore")
        }.format(Date())

        // Convert new goals to Firestore format
        val goalsMap = newGoals.map { mapOf("name" to it.name, "sets" to it.set_required) }

        // Update userGoals in Firestore
        userRef.update("userGoals", goalsMap)
            .addOnSuccessListener {
                userGoals.clear()
                userGoals.addAll(newGoals)
                updateGoalsWithLogData()
                Log.d("Firestore", "User Goals Updated Successfully")
            }
            .addOnFailureListener { Log.e("Firestore", "Error updating user goals", it) }

        // Transaction: Update today's log without re-writing userGoals
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val todaysLog = (snapshot.get("userLogs") as? Map<String, Map<String, Any>>)
                ?.get(todayDate)?.toMutableMap() ?: mutableMapOf()

            // Update only the "goal" field while keeping "completed" & "achieved"
            newGoals.forEach { goal ->
                val existingEntry = todaysLog[goal.name] as? MutableMap<String, Any>
                    ?: mutableMapOf("completed" to 0, "achieved" to false)
                val updatedEntry = existingEntry.toMutableMap()

                updatedEntry["goal"] = goal.set_required // Update the goal value
                Log.d ("updatedEntryCompleted", updatedEntry["completed"].toString())
                Log.d("updatedEntryGoal", updatedEntry["goal"].toString())
                todaysLog[goal.name] = updatedEntry // Put it back into today's log
            }
            // Check if all logs have completed > goal
            val allCompletedMoreThanGoal = todaysLog.all { (_, entry) ->
                val entryMap = entry as? Map<*, *> ?: emptyMap<String, Any>()
                val completed = (entryMap["completed"] as? Number ?: 0).toInt()
                val goal = (entryMap["goal"] as? Number ?: 0).toInt()
                completed >= goal
            }


            // Use Singapore time zone for all calculations.
            val zoneId = ZoneId.of("Asia/Singapore")
            val todayLocal = LocalDate.now(zoneId)
            val yesterday = todayLocal.minusDays(1)

// Retrieve existing streak details
            val streak = snapshot.get("streak") as? Map<String, Any> ?: emptyMap()
            val daysCompleted = (streak["daysCompleted"] as? Number ?: 0).toInt()
            val longestStreak = (streak["longestStreak"] as? Number ?: daysCompleted.toLong()).toInt()
// Parse the stored lastCompletedDate; default to todayLocal if missing.
            val storedLastDateStr = streak["lastCompletedDate"] as? String ?: todayLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val lastCompletedDate = try {
                LocalDate.parse(storedLastDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) {
                todayLocal
            }

            val updatedStreak = mutableMapOf<String, Any>()

            if (!allCompletedMoreThanGoal) {
                // Not all goals are complete.
                if (lastCompletedDate == todayLocal) {
                    // If progress was marked complete today, deduct one day:
                    val newDaysCompleted = (daysCompleted - 1).coerceAtLeast(0)
                    updatedStreak["daysCompleted"] = newDaysCompleted
                    // Also deduct one day from the lastCompletedDate:
                    updatedStreak["lastCompletedDate"] = todayLocal.minusDays(1)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    // Adjust longest streak if necessary.
                    updatedStreak["longestStreak"] = if (daysCompleted == longestStreak)
                        (longestStreak - 1).coerceAtLeast(0)
                    else
                        longestStreak
                } else {
                    // If lastCompletedDate isn't today, leave streak unchanged.
                    updatedStreak["daysCompleted"] = daysCompleted
                    updatedStreak["lastCompletedDate"] = lastCompletedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    updatedStreak["longestStreak"] = longestStreak
                }
            } else {
                // All goals are complete.
                if (lastCompletedDate == yesterday) {
                    // If the last complete day was yesterday, increment the streak.
                    val newDaysCompleted = daysCompleted + 1
                    updatedStreak["daysCompleted"] = newDaysCompleted
                    // Set the lastCompletedDate to today.
                    updatedStreak["lastCompletedDate"] = todayLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    updatedStreak["longestStreak"] = maxOf(longestStreak, newDaysCompleted)
                } else {
                    // Otherwise, leave the streak unchanged.
                    updatedStreak["daysCompleted"] = daysCompleted
                    updatedStreak["lastCompletedDate"] = lastCompletedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    updatedStreak["longestStreak"] = longestStreak
                }
            }

            // Update Firestore with the new streak data.
            transaction.update(userRef, "streak", updatedStreak)


            transaction.update(userRef, "userLogs.$todayDate", todaysLog)


            null
        }
    }



    fun listenToUserExerciseLog() {
        val firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(userId)

        // Remove previous listener to avoid memory leaks
        userLogListener?.remove()

        userLogListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("Firestore", "Error listening to user log", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val logsMap = snapshot.get("userLogs") as? Map<String, Map<String, Map<String, Any>>>
                    ?: emptyMap()

                val sortedLogs = logsMap.map { (date, exerciseData) ->
                    val userGoalsList = exerciseData.map { (exerciseName, details) ->
                        val completed = (details["completed"] as? Number)?.toInt() ?: 0
                        val goalValue = (details["goal"] as? Number)?.toInt() ?: 0
                        UserGoal(
                            name = exerciseName,
                            set_required = goalValue
                        ).apply {
                            set_completed = completed
                        }
                    }

                    ExerciseLog(
                        date = date,
                        exercises = userGoalsList
                    )
                }.sortedByDescending { log ->
                    LocalDate.parse(log.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }

                exerciseLogs.clear()
                exerciseLogs.addAll(sortedLogs)

                updateGoalsWithLogData()
                if (userGoals.isNotEmpty())
                {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Singapore")
                    }.format(Date())
                    val todayLog = exerciseLogs.find { it.date == today }
                    val allGoalsCompleted = userGoals.all { goal ->
                        todayLog?.exercises?.find { it.name == goal.name }?.let { logExercise ->
                            logExercise.set_completed >= logExercise.set_required
                        } ?: false // If goal is missing, consider it incomplete
                    }
                    if (allGoalsCompleted) {
                        updateStreak(userRef)
                    }
                }



            } else {
                Log.d("Firestore", "User document does not exist.")
                exerciseLogs.clear()
            }
        }
    }
    private fun updateStreak(userRef: DocumentReference) {
        val zoneId = ZoneId.of("Asia/Singapore")
        val todayDate = LocalDate.now(zoneId)
        val yesterday = todayDate.minusDays(1)

        // Fetch existing streak data
        userRef.get()
            .addOnSuccessListener { document ->
                val streakMap = document.get("streak") as? Map<String, Any>
                val previousDaysCompleted = (streakMap?.get("daysCompleted") as? Number)?.toInt() ?: 0
                val longestStreak = (streakMap?.get("longestStreak") as? Number)?.toInt() ?: 0
                val lastCompletedDateString = streakMap?.get("lastCompletedDate") as? String ?: ""

                val lastCompletedDate = if (lastCompletedDateString.isNotEmpty()) {
                    LocalDate.parse(lastCompletedDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else null

                // ✅ Prevent duplicate update if last completed date is today
                if (lastCompletedDate == todayDate) {
                    Log.d("Firestore", "Streak already updated today. Skipping update.")
                    return@addOnSuccessListener
                }

                val isConsecutive = lastCompletedDate == yesterday

                val newDaysCompleted = if (isConsecutive) previousDaysCompleted + 1 else 1
                val newLongestStreak = maxOf(longestStreak, newDaysCompleted)

                val updatedStreak = mapOf(
                    "daysCompleted" to newDaysCompleted,
                    "longestStreak" to newLongestStreak,
                    "lastCompletedDate" to todayDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )

                // ✅ Update Firestore with new streak data
                userRef.update("streak", updatedStreak)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Streak updated: $updatedStreak")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating streak", e)
                    }
            }
    }



    private fun updateGoalsWithLogData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Singapore")
        }.format(Date())
        val todayLog = exerciseLogs.find { it.date == today }

        userGoals.forEach { goal ->
            // Look for a matching exercise by name
            val matchingGoal = todayLog?.exercises?.find { it.name == goal.name }
            // If found, set the completed sets; otherwise default to 0
            goal.set_completed = matchingGoal?.set_completed ?: 0
        }
    }

    fun hasEvent(date: LocalDate): Boolean {
        // Convert date to the same string format as stored in exerciseLogs
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        // Find the log for this date (or consider all logs if multiple per day are allowed)
        return exerciseLogs
            .filter { it.date == dateString }
            .any { log ->
                log.exercises.any { exercise ->
                    exercise.set_completed > 0
                }
            }
    }

    // Returns a map of dates for the current month to a boolean flag indicating an event
    fun getEventIndicatorsForMonth(currentMonth: YearMonth): Map<LocalDate, Boolean> {
        return (1..currentMonth.lengthOfMonth()).associate { day ->
            val date = currentMonth.atDay(day)
            date to hasEvent(date)
        }
    }
}

