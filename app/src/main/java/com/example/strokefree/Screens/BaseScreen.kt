package com.example.strokefree.Screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.strokefree.ViewModels.EducationalViewModel
import com.example.strokefree.ViewModels.ExercisesViewModel
import com.example.strokefree.ViewModels.ForumViewModel
import com.example.strokefree.ViewModels.ProfileViewModel
import com.example.strokefree.ui.components.BottomNavigationBar
import com.example.strokefree.ui.components.TopBar

@Composable
fun BaseScreen(
    userID:String ="",
    navController: NavController,
    educationalViewModel: EducationalViewModel,
    exerciseViewModel: ExercisesViewModel,
    forumViewModel: ForumViewModel,
    profileViewModel: ProfileViewModel
){
    var selectedIndex by rememberSaveable { mutableStateOf<Int>(0) }

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = {
            BottomNavigationBar  (
                selectedTab = selectedIndex,
                onTabSelected = { selectedIndex = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (selectedIndex) {
                //TODO: Update the content from these screen
                0 -> HomeScreen(navController, exercisesViewModel = exerciseViewModel, profileViewModel = profileViewModel)
                1 -> RiskScreen(navController)
                2 -> EducationalScreen(navController, viewModel = educationalViewModel)
                3 -> ForumScreen(navController, viewModel = forumViewModel, userID)//TestScreen(navController)
                4 -> ProfileScreen(navController, profileViewModel = profileViewModel)
            }
        }
    }
}


@Preview
@Composable
fun PreviewProgressScreen() {
    val educationalViewModel = EducationalViewModel()
    BaseScreen(userID = "123",
        navController = rememberNavController(),
        educationalViewModel = educationalViewModel,
        exerciseViewModel = ExercisesViewModel(),
        forumViewModel = ForumViewModel(),
        profileViewModel = ProfileViewModel()
    )
}