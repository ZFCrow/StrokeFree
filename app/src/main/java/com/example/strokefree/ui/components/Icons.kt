package com.example.strokefree.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// to be reused in different variations of nav bar
//@Composable
//fun NotificationIcon(
//    navController: NavController
//){
//    IconButton(onClick = {navController.navigate("notification/123") }) {
//        Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notifications")
//    }
//}

@Composable
fun ProfileIcon(
    navController: NavController

){
    IconButton(onClick = {  }) {
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.size(24.dp).clip(CircleShape)
        )
    }
}
@Composable
fun SettingsIcon(
    navController: NavController
){
    IconButton(onClick = {  }) {
        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
    }
}