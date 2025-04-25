package com.example.strokefree.Screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.OnnxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentAnalysisScreen(
    navController: NavController,
    onnxViewModel: OnnxViewModel = viewModel()
) {
    val context = LocalContext.current
    val result by onnxViewModel.result.collectAsState(initial = null)

    val isHighRisk = result == 1 // If 1 -> High Risk, If 0 -> Low Risk

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Stroke Risk Assessment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isHighRisk) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)) // Subtle background color
                .padding(paddingValues),
            contentAlignment = Alignment.Center // Center everything
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Slightly smaller width for better aesthetics
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title
                    Text(
                        text = if (isHighRisk) "High Risk of Stroke Detected!" else "Low Risk of Stroke",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighRisk) Color.Red else Color(0xFF2E7D32), // Red or Green
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(25.dp)) // Add spacing below the icon

                    // 🚨 Large Warning Icon or ✅ Large Checkmark Icon
                    Text(
                        text = if (isHighRisk) "🚨" else "✅",
                        fontSize = 100.sp, // Make it BIG
                        modifier = Modifier.align(Alignment.CenterHorizontally) // Center it
                    )

                    Spacer(modifier = Modifier.height(25.dp)) // Add spacing below the icon

                    if (isHighRisk) {
                        Text(
                            text = "⚠️ Immediate Actions Required:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = """
                                • Stop all activities and rest immediately.
                                • Seek emergency medical attention (Call 995 in Singapore / 911 elsewhere).
                                • Watch for symptoms like numbness, slurred speech, dizziness, or severe headache.
                            """.trimIndent(),
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Emergency Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:995"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(text = "📞 Call Emergency Now", fontSize = 18.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Back to Home Button
                        Button(
                            onClick = {
                                navController.navigate("base") {
                                    popUpTo("risk") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(text = "🏠 Back to Home", fontSize = 18.sp, color = Color.White)
                        }

                    } else {
                        Text(
                            text = "🎉 You are at a low risk of stroke! Maintaining a healthy lifestyle is key to long-term health.",
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "🛡️ Tips for Stroke Prevention:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val tips = listOf(
                            "✅ Eat a balanced diet rich in fruits and vegetables.",
                            "✅ Exercise regularly (at least 30 minutes a day).",
                            "✅ Avoid smoking and limit alcohol consumption.",
                            "✅ Manage stress and monitor blood pressure regularly."
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            tips.forEach { tip ->
                                Text(
                                    text = tip,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Learn More Button
                        Button(
                            onClick = {
                                navController.navigate("base")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                            shape = RoundedCornerShape(30.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(text = "🌿 Learn More About Stroke Prevention \uD83C\uDF3F", fontSize = 14.5.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
