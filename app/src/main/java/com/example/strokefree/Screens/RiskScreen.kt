package com.example.strokefree.Screens

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.RiskAssessmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RiskScreen(
    navController: NavController,
    riskAssessmentViewModel: RiskAssessmentViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("BMI", "Glucose Level")
    val pastAssessments by riskAssessmentViewModel.pastAssessments.collectAsState()

    // Detect orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Fetch data on screen load
    LaunchedEffect(Unit) {
        riskAssessmentViewModel.fetchPastAssessments()
    }

    // Conditionally add vertical scroll modifier only in landscape mode
    val outerModifier = if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(outerModifier)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Risk Assessment", style = MaterialTheme.typography.headlineMedium)

        Button(
            onClick = { navController.navigate("new_risk_assessment") },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Start New Assessment")
        }

        // Tabs for BMI & Glucose Level
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                ) {
                    Text(title)
                }
            }
        }

        val metricData = pastAssessments.map {
            if (selectedTabIndex == 0) it.bmi.toFloat() else it.avgGlucoseLevel.toFloat()
        }

        // Draw Chart using Jetpack Compose Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp)
        ) {
            if (metricData.isNotEmpty()) {
                LineChartCompose(metricData, tabs[selectedTabIndex])
            } else {
                Text(text = "No data available for ${tabs[selectedTabIndex]}.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Past Assessments", style = MaterialTheme.typography.titleMedium)

        if (pastAssessments.isEmpty()) {
            Text("No past assessments found.")
        } else {
            // Past assessments remain in their own scrollable box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)  // Adjust height as needed
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pastAssessments) { assessment ->
                        PastRiskAssessmentItem(
                            navController,
                            assessment.assessmentId,
                            assessment.riskResult,
                            assessment.timestamp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChartCompose(data: List<Float>, title: String) {
    val displayedData = if (data.size > 5) data.takeLast(5) else data  // Show only last 5 entries

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
    ) {
        if (displayedData.size < 2) return@Canvas

        val maxValue = displayedData.maxOrNull() ?: 1f
        val minValue = displayedData.minOrNull() ?: 0f

        val xStep = size.width / (displayedData.size - 1)
        val yStep = size.height / (maxValue - minValue)

        val points = displayedData.mapIndexed { index, value ->
            androidx.compose.ui.geometry.Offset(xStep * index, size.height - ((value - minValue) * yStep))
        }

        // Draw grid lines
        for (i in 1..4) {
            val yPos = size.height - (i * size.height / 4)
            drawLine(
                color = Color.LightGray,
                start = androidx.compose.ui.geometry.Offset(0f, yPos),
                end = androidx.compose.ui.geometry.Offset(size.width, yPos),
                strokeWidth = 1f
            )
        }

        // Draw axis labels
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                textSize = 30f
                color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.CENTER
            }
            // Y-Axis Label (BMI / Glucose Level)
            canvas.nativeCanvas.drawText(title, 40f, size.height / 2, paint)
            // X-Axis Label (Time)
            canvas.nativeCanvas.drawText("Time", size.width / 2, size.height - 10f, paint)
        }

        // Draw line and points
        drawIntoCanvas { canvas ->
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = Color.Blue,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 3f
                )
            }
            for ((index, point) in points.withIndex()) {
                drawCircle(color = Color.Red, radius = 6f, center = point)
                // Display number value near each point
                drawIntoCanvas { textCanvas ->
                    val textPaint = android.graphics.Paint().apply {
                        textSize = 25f
                        color = android.graphics.Color.BLACK
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    textCanvas.nativeCanvas.drawText(
                        displayedData[index].toString(),
                        point.x,
                        point.y - 15f,
                        textPaint
                    )
                }
            }
        }
    }
}

@Composable
fun PastRiskAssessmentItem(
    navController: NavController,
    assessmentId: String,
    riskResult: String,
    timestamp: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Risk Result: $riskResult", style = MaterialTheme.typography.bodySmall)
                Text(text = "Date: ${convertTimestampToDate(timestamp)}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "View details",
                style = MaterialTheme.typography.bodySmall.copy(
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    navController.navigate("past_risk_assessment_screen/$assessmentId")
                }
            )
        }
    }
}

fun convertTimestampToDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
