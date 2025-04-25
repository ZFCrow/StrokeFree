package com.example.strokefree.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strokefree.R
import com.example.strokefree.ViewModels.OnnxViewModel
import com.example.strokefree.ViewModels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRiskAssessmentScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    onnxViewModel: OnnxViewModel = viewModel() // ✅ Declare the ViewModel here
) {
    var isChecked by remember { mutableStateOf(false) }

    // Fetch user profile
    val userProfile by profileViewModel.userProfile
    val loading by profileViewModel.loading
    val error by profileViewModel.error

    // ✅ Ensure user profile is fetched when this screen loads
    LaunchedEffect(Unit) {
        profileViewModel.fetchCurrentUser()
    }

    val userName = userProfile?.name ?: "Loading..." // ✅ Show "Loading..." until data is fetched

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Risk Assessment", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Show loading or error message
                    if (loading) {
                        CircularProgressIndicator()
                    } else if (error.isNotEmpty()) {
                        Text(text = "Error: $error", color = Color.Red)
                    } else {
                        // Circular Profile Placeholder
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            color = Color.LightGray
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile Icon",
                                tint = Color.DarkGray,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ✅ Display correct username
                        Text(
                            text = "Hello, $userName!",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Introduction
                        Text(
                            text = "Welcome to the Stroke Risk Assessment",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "This form is designed to guide you through an evaluation of potential risk factors for stroke. " +
                                    "Here, you'll provide information such as your medical history, lifestyle habits, and any symptoms " +
                                    "you may be experiencing. Your input is vital to generate personalized insights and recommendations " +
                                    "to help you understand and manage your stroke risk effectively.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // Instructions
                        Text(
                            text = "Would you like to start the assessment?",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Note: This assessment is for informational purposes only and does not constitute medical advice. " +
                                    "Please consult a healthcare provider for a professional evaluation.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // Checkbox with terms
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SelectionContainer {
                                Text(
                                    text = "I confirm that the information provided is accurate and complete. " +
                                            "I understand that this information may be used for medical purposes, " +
                                            "and I consent to its processing in accordance with the app’s privacy policy.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Start,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Start Button (at the bottom)
                        Button(
                            onClick = {
                                if (isChecked) {
                                    onnxViewModel.resetResult() // Reset before navigating
                                    navController.navigate("risk_question")
                                }
                            },
                            enabled = isChecked,
                            shape = RoundedCornerShape(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isChecked) MaterialTheme.colorScheme.primary else Color.Gray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp) // Slightly larger button
                        ) {
                            Text(
                                text = "Start Assessment",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
