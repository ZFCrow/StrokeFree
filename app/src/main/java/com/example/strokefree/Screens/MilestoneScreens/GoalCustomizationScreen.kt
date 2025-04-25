package com.example.strokefree.Screens.MilestoneScreens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.ExercisesViewModel
import com.example.strokefree.ViewModels.GoalCustomizationViewModel
import com.example.strokefree.ViewModels.UserInfoViewModel
import com.example.strokefree.classes.UserGoal
import com.example.strokefree.ui.components.TopBarWithBackButton

@Composable
fun GoalCustomizationScreen(
    navController: NavController,
    isRecommended:Boolean,
    exercisesViewModel: ExercisesViewModel,
    goalCustomizationViewModel: GoalCustomizationViewModel
) {
    var isEdit = goalCustomizationViewModel.isEdit
    var exerciseList = exercisesViewModel.recommended
    var userGoals = exercisesViewModel.userGoals

    if (isEdit){
        goalCustomizationViewModel.selectedExercises.putAll(userGoals.associate { it.name to it.set_required })

    }
    else if (isRecommended){
        //TODO to replace the stroke type to dynamically match the stroke type of patient
        goalCustomizationViewModel.selectedExercises.putAll(
            exercisesViewModel.recommended
                .filter { it.strokeType.replace(" ", "").lowercase() == goalCustomizationViewModel.userStrokeType.value.replace(" ", "").lowercase() }
                .associate { it.exerciseName to 3 }
        )
    }
    else{
        goalCustomizationViewModel.selectedExercises.clear()
    }
    Scaffold(
        topBar = {
            TopBarWithBackButton("", navController = navController)
        }
    )
    { innerPadding ->

        if (goalCustomizationViewModel.showDialog) {
            AlertDialog(
                onDismissRequest = { goalCustomizationViewModel.showDialog = false },
                title = { Text("Selected Exercises") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        itemsIndexed(goalCustomizationViewModel.selectedExercises.keys.toList()) { index, exerciseName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exerciseName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "${goalCustomizationViewModel.selectedExercises[exerciseName]} Set(s)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Convert the selectedExercises map to a list of UserGoal objects
                        val userGoalsList =
                            goalCustomizationViewModel.selectedExercises.map { (name, setsRequired) ->
                                UserGoal(name = name, set_required = setsRequired)
                            }
                        // Update Firestore with the new list of user goals
                        exercisesViewModel.updateUserGoals(userGoalsList)
                        goalCustomizationViewModel.showDialog = false
                        navController.popBackStack("progress_tracking/123", inclusive = false)
                    }) {
                        Text("Confirm")
                    }
                },

                dismissButton = {
                    Button(onClick = { goalCustomizationViewModel.showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        if (userGoals.size != 0) {
            goalCustomizationViewModel.isEdit = true
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    isEdit -> "Edit Your Goal"
                    isRecommended -> "Your Recommended Goals"
                    else -> "Pick your goals"
                },
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )
            if (exerciseList.size != 0) {
                val userStrokeType = goalCustomizationViewModel.userStrokeType.value.replace(" ", "").lowercase()

                val recommendedExercises = exercisesViewModel.recommended
                    .filter { it.strokeType.replace(" ", "").lowercase() == userStrokeType }
                    .map { it.exerciseName }

                val reorderedList = exerciseList.sortedByDescending { it.exerciseName in recommendedExercises }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(
                        reorderedList,
                    ) { index, exercise ->

                        Card(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                            colors = if (goalCustomizationViewModel.isExerciseSelected(exercise.exerciseName)) CardDefaults.cardColors(
                                Color(
                                    0xFFC8E6C9
                                )
                            )
                            else CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(8.dp),
                            onClick = {
                                goalCustomizationViewModel.toggleExerciseSelection(exercise)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 16.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 4.dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = exercise.exerciseName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp)
                                )
                                    //TODO if "recommended" based on stroke type
                                    if (exercise.exerciseName in exercisesViewModel.recommended
                                            .filter {
                                                it.strokeType.replace(" ", "").lowercase() ==
                                                        goalCustomizationViewModel.userStrokeType.value.replace(
                                                            " ",
                                                            ""
                                                        ).lowercase()
                                            }
                                            .map { it.exerciseName }
                                    ) {

                                        Text(
                                            text = "Recommended",
                                            style = TextStyle(
                                                fontStyle = FontStyle.Italic,
                                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                            ),


                                            )
                                    }



                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Text(
                                        text = exercise.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp)
                                    )
                                }
                                if (goalCustomizationViewModel.isExerciseSelected(exercise.exerciseName)) {
                                    Column(
                                        modifier = Modifier.weight(0.3f)

                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val interactionSource =
                                                remember { MutableInteractionSource() }

                                            IconButton(
                                                onClick = {
                                                    goalCustomizationViewModel.decrementSets(
                                                        exercise.exerciseName
                                                    )

                                                },
                                                modifier = Modifier.size(36.dp),
                                                interactionSource = interactionSource,


                                                ) {
                                                Icon(
                                                    Icons.Filled.Remove,
                                                    contentDescription = "Reduce"
                                                )

                                            }
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = goalCustomizationViewModel.selectedExercises[exercise.exerciseName].toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                Text(
                                                    "Set(s)",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    goalCustomizationViewModel.incrementSets(
                                                        exercise.exerciseName
                                                    )

                                                },
                                                modifier = Modifier.size(36.dp),
                                                interactionSource = interactionSource,

                                                ) {
                                                Icon(Icons.Filled.Add, contentDescription = "Add")
                                            }
                                        }


                                    }
                                }


                            }

                        }
                    }

                }
                Button(
                    onClick = {
                        goalCustomizationViewModel.showDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text("Done")
                }


            } else {
                CircularProgressIndicator()
                Text("Grabbing Exercise List", style = MaterialTheme.typography.headlineMedium)
            }


        }
    }


}

