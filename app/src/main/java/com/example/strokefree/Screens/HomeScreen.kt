package com.example.strokefree.Screens

import android.content.ClipDescription
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.Uri
import coil3.compose.AsyncImage
import com.example.strokefree.R
import com.example.strokefree.ViewModels.ExercisesViewModel
import com.example.strokefree.ViewModels.ProfileViewModel
import com.example.strokefree.classes.Exercise
import com.example.strokefree.classes.RecommendedExercise
import com.example.strokefree.classes.UserGoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController,
               exercisesViewModel: ExercisesViewModel,
               profileViewModel: ProfileViewModel) {

    val exercises = exercisesViewModel.userGoals

    LaunchedEffect(Unit) {
        profileViewModel.fetchCurrentUser()
        exercisesViewModel.populateViewModelData()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ExerciseSection(
                goals = exercises,
                onClickFunction = {
                    val userID = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    navController.navigate("progress_tracking/$userID")
                }
            )
        }
        item {
            MotivationalSection(navController)
        }
        item {
            RecommendationSection(
                exercisesViewModel.recommended,
                strokeType = profileViewModel.userProfile.value?.strokeType ?: ""
            )
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExerciseSection(onClickFunction: () -> Unit = {}, goals : List<UserGoal>) {
    var overallProgress by remember { mutableStateOf(0f) } // Start at 0

    val calculatedProgress by remember {
        derivedStateOf {
            if (goals.isEmpty()) 0f
            else goals.map { it.percentageCompleted }.average().toFloat()
        }
    }
    LaunchedEffect(calculatedProgress) {
        overallProgress = calculatedProgress // Update progress
    }
    Log.d ("ExerciseSection", "Goals: $goals")
    Log.d ("ExerciseSection", "Overall Progress: $overallProgress")

    // Animate the progress change
    val animatedProgress by animateFloatAsState(
        targetValue = (overallProgress / 100f).coerceAtMost(1f),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )

    var showDetailedProgress by remember { mutableStateOf(false) }

    CustomCard(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable { showDetailedProgress = !showDetailedProgress }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
                    ),
                )
                .padding(16.dp)
        ){
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                //TITLE
                Row(
                ){
                    Text(
                        "Your Progress",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View More",
                        tint = Color.Gray,
                        modifier = Modifier.clickable {
                            onClickFunction()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                BoxWithConstraints {
                    val centerOffset = (maxWidth - 100.dp) / 2
                    val targetOffset = if (showDetailedProgress) 0.dp else centerOffset
                    val animatedOffset by animateDpAsState(
                        targetValue = targetOffset,
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                    )
                    Row (
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Box(
                            modifier = Modifier
                                .offset(x = animatedOffset),
                            contentAlignment = Alignment.Center
                        ){
                            Indicator(animatedProgress, overallProgress)
                        }

                        AnimatedVisibility(
                            visible = showDetailedProgress,
                            enter = fadeIn(animationSpec = tween(1500)) + slideInHorizontally(animationSpec = tween(1500)),
                            exit = fadeOut(animationSpec = tween(1500)) + slideOutHorizontally(animationSpec = tween(1500))
                        ) {
                            val bgColor = when {
                                overallProgress < 30f -> Color(0xFFFFEBEE)  // Light Red
                                overallProgress < 70f -> Color(0xFFFFF3E0)  // Light Amber
                                else -> Color(0xFFE8F5E9)                  // Light Green
                            }

                            val borderColor = when {
                                overallProgress < 30f -> Color(0xFFEF9A9A)  // Darker Red
                                overallProgress < 70f -> Color(0xFFFFB74D)  // Darker Amber
                                else -> Color(0xFF81C784)                  // Darker Green
                            }
                            val textColor = when {
                                overallProgress < 30f -> Color(0xFFC62828)  // Deep Red
                                overallProgress < 70f -> Color(0xFFF57C00)  // Deep Orange
                                else -> Color(0xFF388E3C)                   // Deep Green
                            }

                            Row(
                                modifier = Modifier
                                    .padding(start = 24.dp)
                                    .border(BorderStroke(1.dp, borderColor), shape = RoundedCornerShape(12.dp))
                                    .background(bgColor, RoundedCornerShape(12.dp))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when {
                                        overallProgress < 30f -> "💪 Keep Going!"
                                        overallProgress < 70f -> "🔥 You're Doing Great!"
                                        else -> "🎉 Almost There!"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }

                    }
                }
                // IF SHOW EXERCISES IS TRUE THEN SHOW EXERCISES
                // FOR EACH ITEM IN GOALS , EXERCISE ITEM
                if (showDetailedProgress) {
                    //just do column
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (goal in goals) {
                            GoalItem(goal)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun GoalItem(goal : UserGoal){

    var progressState by remember {
        mutableStateOf(0f)
    }
    LaunchedEffect(goal.percentageCompleted) {
        progressState = goal.percentageCompleted / 100f

    }

    val backgroundColor = if (goal.set_completed >= goal.set_required) {
        Color(0xFF81C784).copy(alpha = 0.7f) // Softened semi-transparent blue for completed
    } else {
        Color(0xFFECEFF1) // Soft grey for incomplete
    }
    val progressBarColor = if (goal.set_completed >= goal.set_required) {
        Color(0xFF66BB6A) // slightly deeper green for completed
    } else {
        Color(0xFF64B5F6) // soft blue for incomplete tasks
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressState,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "Progress Animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) //  Custom height
            .clip(RoundedCornerShape(12.dp)) //  Rounded edges
//            .background(Color(0xFFE0E0E0)), //  Background color (gray)
            .background(backgroundColor), // Dynamic background color
        contentAlignment = Alignment.CenterStart
    ) {
        // Foreground progress bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress) //  Dynamically fill based on progress
                //.background(Color(0xFF1565C0)) //  Foreground color (blue)
                .background(progressBarColor.copy(alpha = 0.8f)) //  Slightly deeper color with transparency
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            // Progress text inside the bar
            Text(
                text = goal.name,
                color = if (progressState > 0.5f) Color.White else Color.Black, // Adjust text color dynamically
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Gray,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
            )
            Text (
                text = "${goal.set_completed}/${goal.set_required} sets",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                    )
                )
        }
    }
}

@Composable
fun MotivationalSection(navController: NavController) {
    CustomCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 16.dp)
            .clickable { navController.navigate("game_screen") }
    ) {
        // Gradient background INSIDE the white card
        Box(
            modifier = Modifier
                .fillMaxSize()

                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.VideogameAsset,
                    contentDescription = "Game Icon",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Play a Game",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Train your brain while having fun",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}



@Composable
fun RecommendationSection(
    recommended: List<RecommendedExercise> = listOf(),
    strokeType : String = ""
) {
    Log.d("Recommended Size(Before Filter)", recommended.size.toString())
    Log.d("StrokeType", strokeType)

    val filteredStrokeType = if (strokeType.isNotEmpty()) {
        strokeType
    } else {
        "Generic Exercises"
    }

    // Filter the recommended exercises based on the stroke type
    val filteredRecommendations = if (strokeType.isNotEmpty()) {
        recommended.filter { it.strokeType == strokeType }
    } else {

        Log.d("StrokeType", "Empty so I am filtering for generic")
        recommended.filter { it.strokeType == "Generic" }
    }
    Log.d("Recommended Size(After Filter)", filteredRecommendations.size.toString())

    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Column{
            Text(
                "Recommended Workout",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "For $filteredStrokeType",
                style = MaterialTheme.typography.bodyMedium
            )
        }


        // Instead of using a LazyColumn, use a simple Column
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Calculate the number of rows (2 items per row)
            val rowCount = filteredRecommendations.size / 2 + filteredRecommendations.size % 2
            for (rowIndex in 0 until rowCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left card
                    CustomVideoCard(
                        filteredRecommendations[rowIndex * 2],
                        Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Right card if available
                    if (rowIndex * 2 + 1 < filteredRecommendations.size) {
                        CustomVideoCard(
                            filteredRecommendations[rowIndex * 2 + 1],
                            Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomVideoCard(
    recommendedItem: RecommendedExercise,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier.aspectRatio(1.5f),
        elevation = 12.dp,
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(recommendedItem.videoUrl))
                    context.startActivity(intent)
                }
        ) {
            // Thumbnail image
            AsyncImage(
                model = getYouTubeThumbnail(recommendedItem.videoUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 0f,
                            endY = 100f  // height of the gradient ?
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Text(
                    text = recommendedItem.exerciseName,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }

            // Centered Play button
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    elevation: Dp = 8.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    gradient: Brush? = null,
    content: @Composable () -> Unit = {}
) {
    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(brush = gradient ?: SolidColor(containerColor))
                .padding(contentPadding)
        ) {
            content()
        }
    }
}


@Composable
fun Indicator(animatedProgress: Float, overallProgress: Float) {

    val progressColor = when {
        overallProgress < 30f -> Color(0xFFFF6B6B) // Red-Pinkish
        overallProgress < 70f -> Color(0xFFFFC75F) // Amber
        else -> Color(0xFF4CAF50)                  // Green
    }

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = Color(0xFFE0E0E0)
        )

        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            color = progressColor,
            strokeCap = StrokeCap.Round
        )

        Text(
            text = "${overallProgress.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}





fun extractVideoId(url: String): String? {
    val regex = Regex("v=([a-zA-Z0-9_-]+)")
    return regex.find(url)?.groupValues?.get(1)
}

fun getYouTubeThumbnail(url: String): String {
    val videoId = extractVideoId(url)
    return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        navController = rememberNavController(),
        exercisesViewModel = ExercisesViewModel(),
        profileViewModel = ProfileViewModel()
    )
}

