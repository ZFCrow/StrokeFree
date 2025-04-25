package com.example.strokefree.Screens.MilestoneScreens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.strokefree.ViewModels.ExercisesViewModel
import com.example.strokefree.ViewModels.GoalCustomizationViewModel
import com.example.strokefree.ViewModels.MilestoneViewModel
import com.example.strokefree.classes.Exercise
import com.example.strokefree.classes.UserGoal
import com.example.strokefree.classes.parseExercisesFromAssets
import com.example.strokefree.ui.components.TopBarWithBackButton
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun ProgressTrackingScreen(
    exerciseViewModel: ExercisesViewModel,
    milestoneViewModel: MilestoneViewModel,
    goalCustomizationViewModel: GoalCustomizationViewModel,
    userId: String = "", navController: NavController
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val context = LocalContext.current
    val exercises = parseExercisesFromAssets(
        context = context, fileName = "strokeData.csv"
    )

    var calendarView by rememberSaveable { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabItems: List<Pair<ImageVector, String>> = listOf(
        Pair(Icons.Filled.List, "List View"), Pair(Icons.Filled.CalendarToday, "Calendar View")
    )
    Scaffold(
        topBar = {
            TopBarWithBackButton(
                "Progress Tracking",
                navController
            )
        },
//        floatingActionButton = { LogExerciseFAB(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = rememberScrollState(),

                    ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressCard(
                navController,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .weight(1f),
                exercises = exercises,
                milestoneViewModel = milestoneViewModel,
                exerciseViewModel = exerciseViewModel,
                goalCustomizationViewModel=goalCustomizationViewModel
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (selectedTabIndex == 0) {
                ExerciseLogSection(
                    exercises,
                    formatter,
                    modifier = Modifier.weight(1f),
                    navController,
                    userId,
                    selectedTabIndex,
                    { selectedTabIndex = it },
                    tabItems,
                    exerciseViewModel = exerciseViewModel
                )
            } else {
                CalendarCard(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    selectedTabIndex,
                    { selectedTabIndex = it },
                    tabItems,
                    exerciseViewModel
                )
            }

        }
    }
}

@Composable
private fun ExerciseLogSection(
    exercises: List<Exercise>,
    formatter: DateTimeFormatter?,
    modifier: Modifier,
    navController: NavController,
    userId: String,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    tabItems: List<Pair<ImageVector, String>> = listOf(),
    exerciseViewModel: ExercisesViewModel

) {


    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.DarkGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth(0.85f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Exercise Log", style = MaterialTheme.typography.titleMedium)

            ViewSwitch(tabItems, selectedTabIndex, onTabSelected)

        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (exerciseViewModel.exerciseLogs.filter { log ->
                        log.exercises.any { exercise -> exercise.set_completed > 0 }
                    }.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize() // Fill the parent's size within the LazyColumn
                                .padding(16.dp),
                            contentAlignment = Alignment.Center // Center the content inside the Box
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,

                                ) {
                                Text(
                                    text = "Start Your Fitness Journey! 🚀",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Every small step counts! Stay consistent and push towards your goals. 💪",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Loop through each day's log
                    itemsIndexed(exerciseViewModel.exerciseLogs) { _, log ->
                        // Partition exercises into two groups:
                        // 1. Targeted (tracked) exercises that are part of userGoals.
                        // 2. Extra effort (untracked) exercises.
                        val (trackedExercises, untrackedExercises) = log.exercises
                            .filter { it.set_completed > 0 }
                            .partition { exerciseViewModel.userGoals.any { goal -> goal.name == it.name } }

                        if (trackedExercises.isNotEmpty() || untrackedExercises.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.elevatedCardElevation(0.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.5.dp, Color.LightGray),

                                ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Date header for the day
                                    Text(
                                        text = "📅 ${log.date}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Section for Targeted Workouts
                                    if (trackedExercises.isNotEmpty()) {
                                        Text(
                                            text = "🎯 Targeted Workouts",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(
                                                start = 8.dp,
                                                top = 8.dp,
                                                bottom = 4.dp
                                            )
                                        )
                                        trackedExercises.forEach { exercise ->
                                            ExerciseLogCard(
                                                exercise,
                                                formatter,
                                                true,
                                                modifier = Modifier
                                                    .padding(
                                                        start = 12.dp,
                                                        top = 4.dp,
                                                        bottom = 4.dp
                                                    )
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                        }
                                    }

                                    // Section for Additional Effort
                                    if (untrackedExercises.isNotEmpty()) {
                                        Text(
                                            text = "💪 Additional Effort",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(
                                                start = 8.dp,
                                                top = 12.dp,
                                                bottom = 4.dp
                                            )
                                        )
                                        untrackedExercises.forEach { exercise ->
                                            ExerciseLogCard(
                                                exercise,
                                                formatter,
                                                false,
                                                modifier = Modifier
                                                    .padding(
                                                        start = 12.dp,
                                                        top = 4.dp,
                                                        bottom = 4.dp
                                                    )
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }
    }
}

@Composable
private fun ExerciseLogCard(
    exercise: UserGoal, formatter: DateTimeFormatter?, tracked: Boolean, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth() // This ensures the Text takes up the full weight's width (i.e., ~50% of parent)
                )
                Text(
                    text = when {

                        exercise.set_required == 0 || !tracked -> "${exercise.set_completed} set(s) completed"
                        exercise.set_completed >= exercise.set_required -> "Completed"
                        else -> "${exercise.set_completed} / ${exercise.set_required} set(s) completed"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}

@Composable
private fun ProgressCard(
    navController: NavController, modifier: Modifier, exercises: List<Exercise>,
    milestoneViewModel: MilestoneViewModel,
    exerciseViewModel: ExercisesViewModel,
    goalCustomizationViewModel: GoalCustomizationViewModel
) {
    var listState = rememberLazyListState()
    LaunchedEffect(milestoneViewModel.daysCompleted.value) {
        listState.animateScrollToItem(index = maxOf(0, milestoneViewModel.daysCompleted.value - 2))
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.DarkGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth(0.85f),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Milestone", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(milestoneViewModel.milestoneGoal.value) { day ->
                    val isMilestone = day % milestoneViewModel.multiple.value == 0 && day != 0
                    val isCompleted = day <= milestoneViewModel.daysCompleted.value

                    val isToday = run {
                        val lastCompleted = milestoneViewModel.lastCompletedDate.value
                        val daysCompleted = milestoneViewModel.daysCompleted.value ?: 0
                        val zoneId = ZoneId.of("Asia/Singapore")

                        val today = LocalDate.now(zoneId)

                        if (lastCompleted != null) {
                            if (lastCompleted != today) {
                                day == daysCompleted + 1
                            } else {
                                day == daysCompleted // Today is the next day after last completed
                            }
                        } else {
                            day == daysCompleted + 1
                        }
                    }


                    val circleSize = when {
                        isMilestone -> 48.dp
                        isToday -> 34.dp
                        else -> 32.dp
                    }
                    val backgroundColor = when {
                        isCompleted -> Color(0xFF4CAF50)
                        isToday -> Color(0xFF1976D2)
                        isMilestone -> Color(0xFFD32F2F)

//                        isToday->Color.Transparent
                        else -> Color(0xFFB0BEC5)
                    }
                    if (day != 0) {


                        Box(
                            modifier = Modifier
                                .size(circleSize)
                                .clip(CircleShape)
                                .background(backgroundColor),

                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> {
                                    Icon(
                                        Icons.Filled.Done,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.padding(9.dp)
                                    )
                                }

                                isMilestone -> {
                                    Icon(
                                        Icons.Filled.Flag,
                                        contentDescription = "Milestone",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }


                                isToday -> {
                                    Icon(
                                        Icons.Filled.AccessTime,
                                        contentDescription = "Today",
                                        tint = Color.White,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

            }
            LaunchedEffect(listState) {
                snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                    .collect { lastVisibleItem ->
                        if (lastVisibleItem != null && lastVisibleItem >= milestoneViewModel.milestoneGoal.value - 8) {
                            milestoneViewModel.milestoneGoal.value += 50 // Increment the milestone goal by 100 more items
                        }
                    }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${milestoneViewModel.daysCompleted.value} Days Completed In A Row",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${milestoneViewModel.nextComingMilestone} Days Left",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Longest Streak: ${milestoneViewModel.longestStreak.value} ",
                    style = MaterialTheme.typography.bodySmall,
                )

            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        //navController.navigate("goal_screen")
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (exerciseViewModel.userGoals.isNotEmpty()) {
                            Text(
                                "Your Goals For Today",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    if (exerciseViewModel.userGoals.isNotEmpty()) {
                                        navController.navigate("goal_customization/edit")

                                    } else if(goalCustomizationViewModel.userStrokeType.value==""){
                                        navController.navigate("goal_customization/edit")

                                    }else {
                                        navController.navigate("goal_setting")
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Setting")

                            }
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (exerciseViewModel.userGoals.isNotEmpty()) {


                            itemsIndexed(exerciseViewModel.userGoals) { _, exercise ->
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val today = dateFormat.format(Date())

                                val doneSets = exercise.set_completed
                                val targettedSets = exercise.set_required
                                val progress =
                                    (doneSets.toFloat() / targettedSets.toFloat()).coerceIn(
                                        0f,
                                        1f
                                    )
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    label = "progressAnimation"
                                )
                                ExerciseProgress(
                                    animatedProgress,
                                    exercise,
                                    doneSets,
                                    targettedSets
                                )
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize(), // Fill the entire space provided by LazyColumn
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if(goalCustomizationViewModel.userStrokeType.value==""){
                                                    navController.navigate("goal_customization/edit")

                                                }else{
                                                    navController.navigate("goal_setting")

                                                }
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.Center,

                                                ) {
                                                Text(
                                                    text = "Set Your Goals Today!",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Your future self will thank you. Dream big, plan smart, and take that first step!",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray
                                                )
                                            }
                                            IconButton(onClick = {
                                                if(goalCustomizationViewModel.userStrokeType.value==""){
                                                    navController.navigate("goal_customization/edit")

                                                }else{
                                                    navController.navigate("goal_setting")

                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Edit Goals",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { navController.navigate("log_exercise/123") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Log Exercise")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Exercise")
                    }
                }
            }

        }


    }
}


@Composable
private fun ExerciseProgress(
    animatedProgress: Float,
    exercise: UserGoal,
    doneSets: Int,
    targettedSets: Int
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
//                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Progress Highlight
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(
                        if (targettedSets != 0) {
                            max(
                                0.05f,
                                animatedProgress
                            )                    // Ensure minimum width is not 0

                        } else {
                            0f // Default full width if `targettedSets == 0`
                        }
                    )
                    .background(Color(0xFFC8E6C9)) // Light green progress background
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodySmall,
                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text(
//                        text = exercise.name,
//                        style = MaterialTheme.typography.labelSmall,
//                        color = Color.Gray
//                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Sets Tracking Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    var text by remember { mutableStateOf("") }
                    var isCompleted = doneSets >= targettedSets && targettedSets != 0
                    if (isCompleted) {
                        text = "Completed"
                    } else if (targettedSets != 0) {
                        text = "${
                            exercise.set_completed
                        } / ${exercise.set_required} Sets"
                    } else {
                        text = "${exercise.set_completed} Sets"
                    }
                    Text(

                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))


                }
            }
        }
    }
}

@Composable
private fun ProgressIcon(
    icon: ImageVector, title: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Progress Icon",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(title, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CalendarCard(
    modifier: Modifier,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabItems: List<Pair<ImageVector, String>> = listOf(),
    exerciseViewModel: ExercisesViewModel
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value
    val zoneId = ZoneId.of("Asia/Singapore")

    val today = LocalDate.now(zoneId)
    val openDialog = remember { mutableStateOf(false) }


    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth(0.85f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calendar View", style = MaterialTheme.typography.titleMedium)

            ViewSwitch(tabItems, selectedTabIndex, onTabSelected)
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        modifier = Modifier.weight(1f)

                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val eventIndicators = exerciseViewModel.getEventIndicatorsForMonth(currentMonth)

            val days = (1..daysInMonth).toList()
            val totalCells = daysInMonth + (firstDayOfMonth - 1)
            for (row in 0 until (totalCells / 7) + 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0 until 7) {

                        val dayIndex = row * 7 + col
                        if (dayIndex >= firstDayOfMonth - 1 && dayIndex - (firstDayOfMonth - 1) < daysInMonth) {
                            val day = days[dayIndex - (firstDayOfMonth - 1)]
                            val date = currentMonth.atDay(day)
                            // Simulate an event indicator (randomized condition; adjust as needed)
                            val hasEvent = eventIndicators[date] ?: false
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        if (hasEvent) {
                                            selectedDate = date
                                            openDialog.value = true

                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {

                                if (date == today) {
                                    // Today's date indicator: a blue circular border
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primaryContainer,
                                                CircleShape
                                            ), contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = if (selectedDate == date) FontWeight.Bold
                                            else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = if (selectedDate == date) FontWeight.Bold
                                            else FontWeight.Normal
                                        )
                                        if (hasEvent) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            selectedDate?.let {
                Text(
                    text = "Selected Date: ${it.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (openDialog.value) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val selectedDateStr = selectedDate!!.format(formatter)
            // Find the exercise log for the selected date.
            val selectedLog = exerciseViewModel.exerciseLogs.find { it.date == selectedDateStr }
            val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

            val displayDateStr = selectedDate!!.format(displayFormatter)

            AlertDialog(
                onDismissRequest = { openDialog.value = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Previous Month")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$displayDateStr")
                    }

                },
                text = {

                    if (selectedLog != null) {
                        LazyColumn {
                            val (goalExercises, nonGoalExercises) = selectedLog.exercises.partition { exercise ->
                                exerciseViewModel.userGoals.any { it.name == exercise.name }
                            }

                            if (goalExercises.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "\uD83C\uDFAF Targeted Workouts",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                itemsIndexed(goalExercises) { _, exercise ->
                                    val doneSets = exercise.set_completed
                                    val targettedSets = exercise.set_required
                                    val progress =
                                        (doneSets.toFloat() / targettedSets.toFloat()).coerceIn(
                                            0f,
                                            1f
                                        )

                                    val animatedProgress by animateFloatAsState(
                                        targetValue = progress,
                                        label = "progressAnimation"
                                    )

                                    if (doneSets > 0) {
                                        ExerciseProgress(
                                            animatedProgress,
                                            exercise,
                                            doneSets,
                                            targettedSets
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }

                            if (nonGoalExercises.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "\uD83D\uDCAA Additional Effort",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                itemsIndexed(nonGoalExercises) { _, exercise ->
                                    val doneSets = exercise.set_completed
//                                    val targettedSets = exercise.set_required

                                    val animatedProgress by animateFloatAsState(
                                        targetValue = 0f, // Untracked exercises have no progress
                                        label = "progressAnimation"
                                    )

                                    if (doneSets > 0) {
                                        ExerciseProgress(
                                            animatedProgress,
                                            exercise,
                                            doneSets,
                                            0
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }


                    } else {
                        Text("No exercise details available for this day.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { openDialog.value = false }) {
                        Text("OK")
                    }
                },
                containerColor = Color.White
            )
        }
    }
}


@Composable
private fun ViewSwitch(
    tabItems: List<Pair<ImageVector, String>>, selectedTabIndex: Int, onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(30.dp)
            .clip(RoundedCornerShape(20.dp)) // ⬅️ Rounded edges for switch effect
            .background(Color.LightGray.copy(alpha = 0.3f)), // ⬅️ Background for unselected area
        horizontalArrangement = Arrangement.Center
    ) {
        tabItems.forEachIndexed { index, item ->
            val isSelected = selectedTabIndex == index
//                        val elevation by animateDpAsState(
//                            targetValue = if (isSelected) 6.dp else 0.dp, label = "tab_elevation"
//                        )
//
//                        // Smooth vertical offset to simulate floating effect
//                        val offsetY by animateDpAsState(
//                            targetValue = if (isSelected) (-2).dp else 0.dp, label = "tab_offset"
//                        )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp)) // ⬅️ Rounded edges for toggle effect
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onTabSelected(index) },
//                                .shadow(elevation, shape = RoundedCornerShape(20.dp)) // Floating effect
//                                .offset(y = offsetY), // Adds vertical lift effect
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(6.dp) // ⬅️ Adjust padding for a clean look
                ) {
                    Icon(
                        imageVector = item.first,
                        contentDescription = item.second,
                        tint = if (isSelected) Color.White else Color.DarkGray,
                        modifier = Modifier.size(18.dp)

                    )
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text(
//                                    text = item.second,
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = if (isSelected) Color.White else Color.DarkGray
//                                )
                }
            }
        }
    }
}

@Composable
fun LogExerciseFAB(navController: NavController) {
    ExtendedFloatingActionButton(
        onClick = { navController.navigate("log_exercise/123") },
        icon = {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Log Exercise"
            )
        },
        text = { Text("Log Exercise") },
        containerColor = MaterialTheme.colorScheme.primary, // Use theme color
        contentColor = Color.White, // Ensure text/icon are visible
        modifier = Modifier
            .padding(16.dp)
    )
}


