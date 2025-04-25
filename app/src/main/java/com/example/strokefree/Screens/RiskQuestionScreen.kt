package com.example.strokefree.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.OnnxViewModel
import com.example.strokefree.ViewModels.ProfileViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskQuestionScreen(
    navController: NavController,
    onnxViewModel: OnnxViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Reset result when entering the screen
    LaunchedEffect(Unit) {
        onnxViewModel.resetResult()
    }

    // Fetch user profile
    val userProfile by profileViewModel.userProfile
    val loading by profileViewModel.loading
    val error by profileViewModel.error

    LaunchedEffect(Unit) {
        profileViewModel.fetchCurrentUser()
    }

    // Assuming userProfile now contains a userId field
    val userId = userProfile?.userID ?: "Loading..."
    val userName = userProfile?.name ?: "Loading..."
    val dob = userProfile?.dob ?: "Loading..."
    val gender = userProfile?.gender ?: "Loading..."
    val age = userProfile?.dob?.let { calculateAge(it) } ?: 60f

    var hypertension by remember { mutableStateOf<String?>(null) }
    var heartDisease by remember { mutableStateOf<String?>(null) }
    var everMarried by remember { mutableStateOf<String?>(null) }
    var avgGlucoseLevel by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf<String?>(null) }
    var smokingStatus by remember { mutableStateOf<String?>(null) }
    var residenceType by remember { mutableStateOf<String?>(null) }

    val totalQuestions = 8
    val answeredQuestions = listOf(
        hypertension, heartDisease, everMarried, avgGlucoseLevel, bmi, workType, smokingStatus, residenceType
    ).count { it != null && it.isNotEmpty() }

    val progress = answeredQuestions.toFloat() / totalQuestions.toFloat()
    val result by onnxViewModel.result.collectAsState()

    LaunchedEffect(result) {
        if (result != null) {
            navController.navigate("riskassessmentanalysis")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Risk Assessment", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Medical History",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(16.dp)
                    ) {
                        Text("👤 Name: $userName", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("🎂 DOB: $dob", fontSize = 16.sp)
                        Text("⚧ Gender: $gender", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item { QuestionSection("1. Do you have hypertension?", listOf("Yes", "No"), hypertension) { hypertension = it } }
                        item { QuestionSection("2. Do you have heart disease?", listOf("Yes", "No"), heartDisease) { heartDisease = it } }
                        item { QuestionSection("3. Have you ever been married?", listOf("Yes", "No"), everMarried) { everMarried = it } }
                        item { InputField("4. What is your average glucose level?", avgGlucoseLevel) { avgGlucoseLevel = it } }
                        item { InputField("5. What is your Body Mass Index (BMI)?", bmi) { bmi = it } }
                        item { QuestionSection("6. What is your work type?", listOf("Never worked", "Private/Self-employed", "Govt_job"), workType) { workType = it } }
                        item { QuestionSection("7. What is your smoking status?", listOf("Formerly smoked", "Never smoked", "Smokes"), smokingStatus) { smokingStatus = it } }
                        item { QuestionSection("8. Where do you live?", listOf("HDB", "Private"), residenceType) { residenceType = it } }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    if (answeredQuestions == totalQuestions) {
                                        onnxViewModel.run(
                                            userId = userId,  // Passing userId instead of userName
                                            age = age,
                                            gender = gender,
                                            hypertension = hypertension,
                                            heartDisease = heartDisease,
                                            everMarried = everMarried,
                                            avgGlucoseLevel = avgGlucoseLevel,
                                            bmi = bmi,
                                            workType = workType,
                                            smokingStatus = smokingStatus,
                                            residenceType = residenceType
                                        )
                                    }
                                },
                                enabled = answeredQuestions == totalQuestions,
                                shape = RoundedCornerShape(30.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (answeredQuestions == totalQuestions) MaterialTheme.colorScheme.primary else Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth().height(60.dp)
                            ) {
                                Text(text = "Submit", fontSize = 18.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var isError by remember { mutableStateOf(false) }

    Column {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = {
                val sanitized = it.filter { char -> char.isDigit() || char == '.' }
                val parsed = sanitized.toFloatOrNull()
                isError = parsed == null || parsed <= 0f
                onValueChange(sanitized)
            },
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            supportingText = {
                if (isError && value.isNotBlank()) {
                    Text("Please enter a number greater than 0", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true
        )
    }
}

fun calculateAge(dob: String): Float {
    Log.d("Debug", "Received DOB: $dob")
    return try {
        val parts = dob.split("/")
        if (parts.size != 3) throw IllegalArgumentException("Invalid DOB format")
        val birthYear = parts[2].toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val age = (currentYear - birthYear).toFloat()
        Log.d("Debug", "Calculated Age: $age")
        age
    } catch (e: Exception) {
        Log.e("Debug", "Error calculating age: ${e.message}")
        60f
    }
}

@Composable
fun QuestionSection(title: String, options: List<String>, selectedOption: String?, onOptionSelected: (String) -> Unit) {
    Column {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Column(Modifier.selectableGroup()) {
            options.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (selectedOption == option),
                            onClick = { onOptionSelected(option) }
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = (selectedOption == option), onClick = { onOptionSelected(option) })
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
