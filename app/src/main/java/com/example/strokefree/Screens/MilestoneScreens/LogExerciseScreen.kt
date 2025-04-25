package com.example.strokefree.Screens.MilestoneScreens

import android.app.DatePickerDialog
import android.util.Log
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.strokefree.R
import com.example.strokefree.Screens.extractVideoId
import com.example.strokefree.ViewModels.ExerciseTrackingViewModel
import com.example.strokefree.ViewModels.ExercisesViewModel
import com.example.strokefree.ui.components.TopBarWithBackButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExerciseScreen(
    userId: String,
    navController: NavController,
    exerciseTrackingViewModel: ExerciseTrackingViewModel,
    exerciseViewModel: ExercisesViewModel
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    var showDatePicker by remember { mutableStateOf(false) }
    val todaysDate = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var selectedDate by remember { mutableStateOf(todaysDate.format(formatter)) }


    val context = LocalContext.current
//    val exerciseOptions =
//        parseExercisesFromAssets(context, "Updated_Stroke_Rehabilitation_Exercises__Refined_.csv")
    var expanded by remember { mutableStateOf(false) }

    var durationText by remember { mutableStateOf<String>("") }

    var calorieBurnt by remember { mutableStateOf<Int>(0) }
    var calorieBurntText by remember { mutableStateOf<String>("") }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Create a new LocalDateTime and format it
            val newDate = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
            selectedDate = newDate.format(formatter)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = { TopBarWithBackButton("", navController) },

        ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(25.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),

                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Text("Start Exercising", style = MaterialTheme.typography.displaySmall)

                    Spacer(modifier = Modifier.height(32.dp))
//                    OutlinedButton(onClick = { showDatePicker = true }) {
//                        Text(text = if (selectedDate.isEmpty()) "Select Date" else selectedDate)
//                    }


//                    // Date Section
//                    OutlinedButton(
//                        onClick = { datePickerDialog.show() },
//                        shape = RoundedCornerShape(24.dp),
//                        border = ButtonDefaults.outlinedButtonBorder,
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
//                        modifier = Modifier
//                            .fillMaxWidth(0.7f)
//                            .height(50.dp)
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center,
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.CalendarMonth,
//                                contentDescription = "Calendar Icon",
//                                tint = MaterialTheme.colorScheme.primary,
//                                modifier = Modifier.size(22.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = selectedDate,
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color.Black
//                            )
//                        }
//                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Exercise Drop Down
                    OutlinedButton(
                        onClick = { exerciseTrackingViewModel.toggleExerciseDialog(true) },
                        shape = RoundedCornerShape(24.dp),
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        enabled = !exerciseTrackingViewModel.hasStarted
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (exerciseTrackingViewModel.selectedExercise.isNotEmpty()) exerciseTrackingViewModel.selectedExercise else "Pick an Execise",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (!exerciseTrackingViewModel.hasStarted) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown Icon",
                                    tint = Color(0xFF6200EE),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }



                    if (exerciseTrackingViewModel.showExerciseDialog) {
                        AlertDialog(
                            modifier = Modifier.fillMaxHeight(0.4f),
                            onDismissRequest = {
                                exerciseTrackingViewModel.toggleExerciseDialog(
                                    false
                                )
                            },
                            title = { Text("Select Exercise") },
                            text = {
                                LazyColumn {
                                    if (!exerciseViewModel.userGoals.isEmpty()) {
                                        item {
                                            Text("Your Goals", fontWeight = FontWeight.Bold)
                                        }

                                        itemsIndexed(exerciseViewModel.userGoals) { index, exercise ->
                                            Text(
                                                text = exercise.name,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                                    .clickable {
                                                        exerciseTrackingViewModel.updateSelectedExercise(
                                                            exercise.name
                                                        )
                                                        exerciseTrackingViewModel.toggleExerciseDialog(
                                                            false
                                                        )
                                                    }
                                            )
                                        }
                                    }
                                    item {
                                        Text("Other Exercises", fontWeight = FontWeight.Bold)

                                    }
                                    val otherList =
                                        exerciseViewModel.recommended.filterNot { recommendedExercise ->
                                            exerciseViewModel.userGoals.any { it.name == recommendedExercise.exerciseName }
                                        }.sortedBy { it.exerciseName }
                                    itemsIndexed(otherList) { index, exercise ->
                                        Text(
                                            text = exercise.exerciseName,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                                .clickable {
                                                    exerciseTrackingViewModel.updateSelectedExercise(
                                                        exercise.exerciseName
                                                    )
                                                    exerciseTrackingViewModel.toggleExerciseDialog(
                                                        false
                                                    )
                                                }
                                        )
                                    }


//                                    itemsIndexed(exerciseOptions.subList(0, 3)) { index, exercise ->
//                                        Row() {
//                                            Text(
//                                                text = exercise.exercise_name,
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .padding(8.dp)
//                                                    .clickable {
//                                                        selectedExercise =
//                                                            exercise.exercise_name
//                                                        showExerciseDialog = false
//                                                    }
//                                            )
//
//                                        }
//
//
//                                    }
//                                    item {
//                                        Text("Other Exercises", fontWeight = FontWeight.Bold)
//
//                                    }
//                                    itemsIndexed(
//                                        exerciseOptions.subList(
//                                            3,
//                                            exerciseOptions.size
//                                        )
//                                    ) { index, exercise ->
//                                        Row() {
//                                            Text(
//                                                text = exercise.exercise_name,
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .padding(8.dp)
//                                                    .clickable {
//                                                        selectedExercise =
//                                                            exercise.exercise_name
//                                                        showExerciseDialog = false
//                                                    }
//                                            )
//
//                                        }


//                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    exerciseTrackingViewModel.toggleExerciseDialog(false)
                                }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!exerciseTrackingViewModel.hasStarted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {

                                    if (exerciseTrackingViewModel.numOfSetsRequired>1){
                                        exerciseTrackingViewModel.decrementSets()

                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Remove,
                                    contentDescription = "Decrement",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${exerciseTrackingViewModel.numOfSetsRequired}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text("Set(s)", style = MaterialTheme.typography.labelMedium)
                            }

                            IconButton(
                                onClick = {
                                    exerciseTrackingViewModel.incrementSets()

                                }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Increment",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

//                    DropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false },
//                        modifier = Modifier.fillMaxWidth(0.6f)
//                    ) {
//                        exerciseOptions.forEach { exercise ->
//                            DropdownMenuItem(
//                                text = { Text(exercise) },
//                                onClick = {
//                                    selectedExercise = exercise
//                                    expanded = false
//                                },
//                            )
//                        }
//                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (exerciseTrackingViewModel.selectedExercise!=""){
                        val videoUrl = exerciseViewModel.recommended.find { it.exerciseName == exerciseTrackingViewModel.selectedExercise }
                            ?.videoUrl ?: "" // Provide a default empty string if not found


                            YouTubePlayerLoop(videoUrl)



                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (exerciseTrackingViewModel.isExerciseCompleted) {

                        CompletedExerciseDialog(

                            onBack = {
                                exerciseTrackingViewModel.resetTimer()
                                exerciseTrackingViewModel.selectedExercise=""
                                navController.popBackStack()
                            },
                            onContinue = {
                                exerciseTrackingViewModel.resetTimer()
                            }
                        )
                    } else if (exerciseTrackingViewModel.hasStarted) {
                        val colorChoice = when {
                            !exerciseTrackingViewModel.isPaused -> Color.Gray
                            exerciseTrackingViewModel.secText <= 10 -> Color.Red
                            else -> Color.Black
                        }
                        when {
                            // Rest timer branch:
                            exerciseTrackingViewModel.isRestTimer -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Great Job!",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0) // Dark Blue
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Keep It Going",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Next Set Coming Up in",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "%02d:%02d".format(
                                            exerciseTrackingViewModel.minText,
                                            exerciseTrackingViewModel.secText
                                        ),
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colorChoice
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            // Active exercise timer branch:
                            else -> {

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Timer",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Set ${exerciseTrackingViewModel.currentSet} of ${exerciseTrackingViewModel.numOfSetsRequired}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "%02d:%02d".format(
                                            exerciseTrackingViewModel.minText,
                                            exerciseTrackingViewModel.secText
                                        ),
                                        style = MaterialTheme.typography.displayLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colorChoice
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }


                    }
                    // Play and pause button
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (exerciseTrackingViewModel.hasStarted) {
                            Surface(
                                modifier = Modifier
                                    .size(56.dp) // Adjust size
                                    .clickable(onClick = {
                                        exerciseTrackingViewModel.resetTimer()
                                    }), // Click action
                                shape = CircleShape,
                                color = Color.Red, // Green background color
                                shadowElevation = 6.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Cancel,
                                        contentDescription = "Reset",
                                        tint = Color.White, // White play icon
                                        modifier = Modifier.size(32.dp) // Adjust icon size
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        Surface(
                            modifier = Modifier
                                .size(if (!exerciseTrackingViewModel.hasStarted) 120.dp else 56.dp) // Adjust size
                                .clickable(onClick = {
                                    val inflater = LayoutInflater.from(context)
                                    val layout = inflater.inflate(R.layout.error_toast, null)
                                    val textView = layout.findViewById<TextView>(R.id.toast_text)

                                    if (exerciseTrackingViewModel.selectedExercise!="" && exerciseTrackingViewModel.numOfSetsRequired>0) {
                                        exerciseTrackingViewModel.toggleTimer()
                                    }
                                    else if (exerciseTrackingViewModel.selectedExercise==""){
                                        textView.text = "Please select an exercise"

                                        Toast(context).apply {
                                            duration = Toast.LENGTH_LONG
                                            view = layout
                                            show()
                                        }
                                    }
                                    else if (exerciseTrackingViewModel.numOfSetsRequired==0){
                                        textView.text = "Please input number of sets"
                                        Toast(context).apply {
                                            duration = Toast.LENGTH_LONG
                                            view = layout
                                            show()
                                        }
                                    }

                                }), // Click action
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary, // Green background color
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = if (!exerciseTrackingViewModel.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                    contentDescription = "Play",
                                    tint = Color.White, // White play icon
                                    modifier = Modifier.size(if (!exerciseTrackingViewModel.hasStarted) 100.dp else 32.dp) // Adjust icon size
                                )
                            }
                        }

                    }


//                    OutlinedTextField(
//                        value = durationText,
//                        onValueChange = { newValue ->
//                            if (newValue.all { it.isDigit() }) {
//                                durationText = newValue
//                            }
//                        },
//                        label = { Text("Duration (mins)") },
//                        modifier = Modifier.fillMaxWidth(),
//                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    if (!exerciseTrackingViewModel.hasStarted) {

                        Text(
                            "Click the play button to start the exercise",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
//                    OutlinedTextField(
//                        value = calorieBurntText,
//                        readOnly = true,
//                        onValueChange = {calorieBurntText=it},
//                        label = { Text("Calorie Burnt") },
//                        modifier = Modifier.fillMaxWidth(),
//                        colors = TextFieldDefaults.colors(
//                            unfocusedContainerColor = Color.LightGray.copy(alpha = 0.3f), // Light gray background
//                            focusedContainerColor = Color.LightGray.copy(alpha = 0.3f), // Avoid focus effect
//                            disabledContainerColor = Color.LightGray.copy(alpha = 0.3f), // Keep consistency when disabled
//                            unfocusedTextColor = Color.DarkGray, // Dimmed text color
//                            focusedTextColor = Color.DarkGray, // Prevent color change on focus
//                            disabledTextColor = Color.DarkGray // Keep text consistent
//                        )
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = {},
//                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
//                    ) {
//                        Text("Add Now")
//                    }
                }
            }


        }
    }


}

@Composable
fun CompletedExerciseDialog(onContinue: () -> Unit, onBack: () -> Unit) {
    // Using AlertDialog to pop up a dialog for a completed exercise.
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = "Exercise Completed!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = "You've finished all sets. What would you like to do next?")
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(modifier = Modifier.fillMaxWidth(), onClick = onContinue) {
                    Text(text = "Continue with Another Exercise")
                }
            }

        },
        dismissButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),

                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                )
                {
                    Text(text = "Back to Milestone")
                }
            }

        }
    )
}

@Composable
fun YouTubePlayerLoop(url: String) {
    val videoId = extractYouTubeVideoId(url) ?: return
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        // Load the video when the player is ready
                        youTubePlayer.loadVideo(videoId, 0f)
                        // Auto-loop: listen for video end
                        youTubePlayer.addListener(object : YouTubePlayerListener {
                            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                                if (state == PlayerConstants.PlayerState.ENDED) {
                                    youTubePlayer.loadVideo(videoId, 0f)
                                }
                            }
                            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                Log.e("YouTubePlayer", "Error: $error")
                            }
                            override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
                            override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
                            override fun onReady(youTubePlayer: YouTubePlayer) {}
                            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {}
                            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {}
                            override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
                            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
                            override fun onApiChange(youTubePlayer: YouTubePlayer) {}
                        })
                    }
                })
            }
        },
        update = { youTubePlayerView ->
            youTubePlayerView.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoId, 0f)
                }
            })
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}




fun extractYouTubeVideoId(url: String): String? {
    val regex = ".*(?:youtu.be/|v/|e/|watch\\?v=|watch\\?.+&v=)([^#&?]*).*".toRegex()
    return regex.find(url)?.groupValues?.get(1)
}

//@Preview
//@Composable
//fun PreviewLogExerciseScreen() {
//    LogExerciseScreen("", rememberNavController())
//
//}