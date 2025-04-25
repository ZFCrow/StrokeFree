package com.example.strokefree.Screens

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.strokefree.MainActivity
import com.example.strokefree.ViewModels.ProfileViewModel
import com.example.strokefree.classes.UserProfile
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.LaunchedEffect
import com.example.strokefree.classes.RiskFactor


@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
) {
    val userProfile by profileViewModel.userProfile
    val loading by profileViewModel.loading
    val error by profileViewModel.error
    val userID by profileViewModel.userID

    LaunchedEffect(Unit) {
        val updated = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("profileUpdated") ?: false

        if (updated) {
            userID?.let { profileViewModel.fetchUserProfile(it) }

            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("profileUpdated")
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                } else if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else if (userProfile != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ProfileHeader(userProfile!!)

                        // Edit button positioned at the top-right
                        IconButton(
                            onClick = { navController.navigate("edit_profile/${userID}") },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Text(
                        "No user data available",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            val riskFactors = profileViewModel.getUserRiskFactors()

            item {
                Text(
                    text = "Stroke Risk Factors",
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            if (riskFactors.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "No known medical conditions selected.",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Stroke can occur due to various factors even without pre-existing illnesses. " +
                                        "Keep an eye on symptoms like sudden numbness, confusion, trouble speaking or walking. " +
                                        "Maintaining a healthy lifestyle, regular check-ups, and managing blood pressure are key to prevention.",
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                items(riskFactors) { factor ->
                    RiskFactorCard(factor)
                }
            }

            item {
                val activity = LocalActivity.current

                Spacer(modifier = Modifier.height(16.dp))
                LogoutButton {

                    profileViewModel.logout {
                        //to destroy the activity and exercise view model to reset it
                        val intent = Intent(activity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        if (activity != null) {
                            activity.startActivity(intent)
                        }
                        if (activity != null) {
                            activity.finish()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


// Profile Header Section
@Composable
fun ProfileHeader(userProfile: UserProfile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            val bitmap = try {
                val bytes = Base64.decode(userProfile.imageURL, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = userProfile.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = "DOB: ${userProfile.dob}", fontSize = 16.sp, color = Color.Gray)
        Text(text = "Gender: ${userProfile.gender}", fontSize = 16.sp, color = Color.Gray)
        Text(
            text = "Blood Type: ${if (userProfile.bloodType.isNotBlank()) userProfile.bloodType else "Not Set"}",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Text(text = "Phone: ${userProfile.phoneNumber}", fontSize = 16.sp, color = Color.Gray)
    }
}

// Logout Button
@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
    ) {
        Text(text = "Logout", fontSize = 18.sp, color = Color.White)
    }
}

@Composable
fun RiskFactorCard(factor: RiskFactor) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = factor.condition,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Severity: ${factor.severity}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = factor.impactDescription,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}


