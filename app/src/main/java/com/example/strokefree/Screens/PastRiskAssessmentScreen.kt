package com.example.strokefree.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.RiskAssessmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastRiskAssessmentScreen(
    navController: NavController,
    assessmentId: String,
    riskAssessmentViewModel: RiskAssessmentViewModel = viewModel()
) {
    var assessmentDetails by remember { mutableStateOf<Map<String, Any>?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Fetch the assessment data when the screen loads
    LaunchedEffect(assessmentId) {
        riskAssessmentViewModel.fetchAssessmentById(assessmentId) { result, errorMsg ->
            if (result != null) {
                assessmentDetails = result
                loading = false
            } else {
                error = errorMsg
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Past Risk Assessment", style = MaterialTheme.typography.headlineSmall) },
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
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
            } else if (assessmentDetails != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Past Risk Assessment",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        AssessmentDetailItem(label = "📅 Date", value = formatDate(assessmentDetails!!["timestamp"] as Long))
                        AssessmentDetailItem(label = "🎂 Age", value = assessmentDetails!!["age"].toString())
                        AssessmentDetailItem(label = "⚧ Gender", value = assessmentDetails!!["gender"].toString())

                        // ✅ NEW: Ever Married Field
                        AssessmentDetailItem(label = "💍 Married", value = assessmentDetails!!["everMarried"].toString())

                        AssessmentDetailItem(label = "🩸 Hypertension", value = assessmentDetails!!["hypertension"].toString())
                        AssessmentDetailItem(label = "💖 Heart Disease", value = assessmentDetails!!["heartDisease"].toString())
                        AssessmentDetailItem(label = "🚬 Smoking Status", value = assessmentDetails!!["smokingStatus"].toString())
                        AssessmentDetailItem(label = "🏡 Residence Type", value = assessmentDetails!!["residenceType"]?.toString() ?: "Unknown")

                        // ✅ BMI & Glucose Level Fields
                        AssessmentDetailItem(label = "⚖️ BMI", value = assessmentDetails!!["bmi"].toString() + " kg/m²")
                        AssessmentDetailItem(label = "🩸 Glucose Level", value = assessmentDetails!!["avgGlucoseLevel"].toString() + " mg/dL")

                        Spacer(modifier = Modifier.height(16.dp))

                        // Risk Result Box
                        val isHighRisk = assessmentDetails!!["riskResult"].toString() == "High Risk"
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighRisk) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isHighRisk) "🚨 High Risk Detected" else "✅ Low Risk",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHighRisk) Color.Red else Color(0xFF2E7D32)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (isHighRisk) {
                                        "⚠️ Immediate medical consultation is advised."
                                    } else {
                                        "🎉 Keep maintaining a healthy lifestyle!"
                                    },
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Risk Screen", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ✅ Function to format timestamp into a readable date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// ✅ Composable for displaying each detail item
@Composable
fun AssessmentDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Text(text = value, fontSize = 16.sp)
    }
}
