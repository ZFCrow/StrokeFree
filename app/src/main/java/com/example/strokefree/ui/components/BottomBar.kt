package com.example.strokefree.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier =Modifier.shadow(8.dp)

    ) {
        val items = listOf(
            "Home" to Icons.Filled.Home,
            "Risk" to Icons.Filled.Warning,
            "Education" to Icons.Filled.Book,
            "Forum" to Icons.Filled.Chat,
            "Profile" to Icons.Filled.AccountCircle
        )

        items.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                icon = {
                    Icon(imageVector = icon, contentDescription = label)
                },
                label = { Text(label) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = Color.DarkGray,
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color.DarkGray,
                    indicatorColor = Color(0xFFEDE7F6)
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomNavBar() {
    var selectedTab by remember { mutableStateOf(0) }
    BottomNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
}
