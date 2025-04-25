package com.example.strokefree.ViewModels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.concurrent.timer

class ExerciseTrackingViewModel: ViewModel() {
    var userID by   mutableStateOf("")



    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val currentUser = auth.currentUser

        if (currentUser != null ) {
            userID = currentUser.uid

        }
    }

    init {

        // Attach the auth state listener
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        // Remove the listener to avoid memory leaks
        firebaseAuth.removeAuthStateListener(authListener)
    }
    var selectedExercise by   mutableStateOf("")

    var showExerciseDialog by  mutableStateOf(false)
    var hasStarted by   mutableStateOf(false)
    var numOfSetsRequired by mutableStateOf(0)
    var minText by mutableStateOf(0)
    var secText by mutableStateOf(0)
    var durationLeft by mutableStateOf(5*1000L)
    var currentSet by mutableStateOf(1)
    var isSetCompleted by mutableStateOf(false)
    var isExerciseCompleted by mutableStateOf(false)
    var isRestTimer by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    // val userID =getCurrentUserId()

    private var countDownTimer: CountDownTimer? = null
    private var workoutDurationInMillis by mutableStateOf(5000L)
    fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid
    }
    // Function to update selectedExercise
    fun updateSelectedExercise(exercise: String) {
        selectedExercise = exercise
    }

    // Function to toggle showExerciseDialog
    fun toggleExerciseDialog(show: Boolean) {
        showExerciseDialog = show
    }

    fun updateCurrentSetRequired(setRequired:Int){
        numOfSetsRequired=setRequired
    }
    fun startTimer() {
        if (!hasStarted){

            hasStarted=true
        }
        countDownTimer?.cancel() // Cancel any existing timer

        countDownTimer = object : CountDownTimer(durationLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                durationLeft = millisUntilFinished
                minText = (millisUntilFinished / 60000).toInt() // convert ms to minutes
                secText = ((millisUntilFinished % 60000) / 1000).toInt() // convert remaining ms to seconds
            }

            override fun onFinish() {
                if (isRestTimer) {
                    // Rest timer finished: now increment the set count for the next workout round.
                    currentSet++
                    isRestTimer = false
                    isSetCompleted = false
                    // Reset durationLeft for the workout timer.
                    durationLeft = workoutDurationInMillis
                    startTimer() // Start the workout timer for the new set.
                } else {
                    // Normal exercise timer finished.
                    isSetCompleted = true
                    updateUserExerciseLog()

                    if (currentSet == numOfSetsRequired) {
                        // Final set: flag exercise as completed immediately.
                        isExerciseCompleted = true

                        hasStarted=false
                        isPaused=false
                    } else {
                        // Not the final set: start a 3-second rest timer.
                        isRestTimer = true
                        durationLeft = 3000L // 3 seconds in milliseconds.
                        startTimer() // Start the rest timer.
                    }
                }
            }
        }.start()

    }

    // Pause timer
    fun pauseTimer() {
        countDownTimer?.cancel()
    }

    // Reset Timer
    fun resetTimer() {

        countDownTimer?.cancel()
        durationLeft = workoutDurationInMillis
        minText = 0
        secText = 0
        hasStarted = false
        currentSet = 1
        isSetCompleted = false
        isRestTimer = false
        isExerciseCompleted=false
        isPaused=false
    }
    // Function to toggle timerStarted
    fun toggleTimer() {
        isPaused = !isPaused
        if (isPaused){
            startTimer()
        }
        else{
            pauseTimer()
        }
    }

    fun incrementSets() {
        numOfSetsRequired++
    }
    fun decrementSets() {
        numOfSetsRequired--

    }


    fun updateUserExerciseLog() {
        val firestore = FirebaseFirestore.getInstance()
        val userRef =firestore.collection("users").document(userID)

        // Get today's date in the format "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Singapore")
        }
        val todayDate = dateFormat.format(Date())


        if (userRef != null) {
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // 1. Fetch the existing 'Log' map, or create empty if it doesn't exist.
                        // Structure: Date -> (Exercise Name -> (Field -> Value))
                        val existingLogs = document.get("userLogs") as? Map<String, Map<String, Map<String, Any>>>
                            ?: emptyMap()

                        // 2. Get (or create) the sub-map for today's date
                        val todayLogsMutable = existingLogs[todayDate]?.toMutableMap() ?: mutableMapOf<String, Map<String, Any>>()

                        // 3. Get the current data for the selected exercise, if any.
                        // If it doesn't exist, start with default values.
                        val userGoalsList = document.get("userGoals") as? List<Map<String, Any>> ?: emptyList()
                        // Find a goal matching the selected exercise.
                        val matchingGoalEntry = userGoalsList.find { it["name"] == selectedExercise }
                        val requiredSets = (matchingGoalEntry?.get("sets") as? Number)?.toInt() ?: 0
                        val exerciseData: MutableMap<String, Any> = todayLogsMutable[selectedExercise]?.toMutableMap()
                            ?: mutableMapOf<String, Any>(
                                "achieved" to false,
                                "completed" to 0,
                                "goal" to requiredSets
                            )

                        // 4. Update the "completed" count by incrementing it.
                        val currentCompleted = (exerciseData["completed"] as? Number)?.toInt() ?: 0
                        exerciseData["completed"] = currentCompleted + 1

                        // (Optional) Update "achieved" based on a condition:
                        val goalValue = (exerciseData["goal"] as? Number)?.toInt() ?: 0
                        if (goalValue > 0 && (currentCompleted + 1) >= goalValue) {
                            exerciseData["achieved"] = true
                        }

                        // 5. Put the updated exercise data back into today's logs
                        todayLogsMutable[selectedExercise] = exerciseData

                        // 6. Put the updated today's logs back into the overall Log map.
                        val updatedLogs = existingLogs.toMutableMap()
                        updatedLogs[todayDate] = todayLogsMutable

                        // 7. Prepare final data and merge into Firestore
                        val finalData = mapOf("userLogs" to updatedLogs)
                        userRef.set(finalData, SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d("Firestore", "Exercise log updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating log", e)
                            }
                    } else {
                        Log.d("Firestore", "User document does not exist.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching user document", e)
                }
        }
    }



}
