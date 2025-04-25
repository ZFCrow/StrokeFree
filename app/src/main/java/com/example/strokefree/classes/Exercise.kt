package com.example.strokefree.classes

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime

data class Exercise(
    val exercise_name: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val set_required: Int=0,
    val duration: Int=0, // Duration in minutes
    val stroke_type: String="",
    val description: String="",
    val focus_area: String="",
    val video_url: String="",
    val caloriesBurnt: Int = 0,  // Calories burnt
    var isCompleted : Boolean = false

)

fun parseExercisesFromAssets(context: Context, fileName: String): List<Exercise> {
    val exercises = mutableListOf<Exercise>()

    try {
        // Open CSV file from assets
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.useLines { lines ->
            lines.drop(1).forEach { line -> // Drop the header row
                val tokens = line.split(",") // Split CSV by comma

                if (tokens.size >= 5) { // Ensure all fields exist
                    val strokeType = tokens[0].trim()
                    val exerciseName = tokens[1].trim()
                    val description = tokens[2].trim()
                    val focusArea = tokens[3].trim()
                    val videoUrl = tokens[4].trim()

                    // Create an Exercise object (default duration & caloriesBurnt)
                    val exercise = Exercise(
                        stroke_type = strokeType,
                        exercise_name = exerciseName,
                        description = description,
                        focus_area = focusArea,
                        video_url = videoUrl,
                        timestamp = LocalDateTime.now(),
                    )
                    exercises.add(exercise)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace() // Log error if file read fails
    }

    return exercises
}


//Stroke Type,Exercise Name,Description,Focus Area,Video URL
data class RecommendedExercise(
    val strokeType: String = "",
    val exerciseName: String = "",
    val description: String = "",
    val focusArea: String = "",
    val videoUrl: String = ""
)

data class UserGoal(
    val name: String = "",
    val set_required: Int,
    val completed : Boolean= false,
    private val _set_completed: MutableState<Int> = mutableStateOf(0)
) {
    var set_completed: Int by _set_completed

    val percentageCompleted: Float
        get() = if (set_completed <= set_required) (set_completed.toFloat() / set_required.toFloat()) * 100 else 100f

}

data class ExerciseLog(
    val date: String = "",
    val exercises: List<UserGoal> = emptyList()
)



