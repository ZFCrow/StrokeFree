package com.example.strokefree

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.strokefree.Screens.HomeScreen
import com.example.strokefree.Screens.LoginScreen
import com.example.strokefree.Screens.BaseScreen
import com.example.strokefree.Screens.ContentDetailScreen
import com.example.strokefree.Screens.EditProfileScreen
import com.example.strokefree.Screens.NewPostScreen
import com.example.strokefree.Screens.ForumScreen
import com.example.strokefree.Screens.MilestoneScreens.GoalCustomizationScreen
import com.example.strokefree.Screens.MilestoneScreens.GoalSettingScreen
import com.example.strokefree.Screens.MilestoneScreens.LogExerciseScreen
import com.example.strokefree.Screens.PostDetailScreen
import com.example.strokefree.Screens.ProfileScreen
import com.example.strokefree.Screens.MilestoneScreens.ProgressTrackingScreen
import com.example.strokefree.Screens.SignUpScreen
import com.example.strokefree.Screens.UserInfoScreen
import com.example.strokefree.Screens.EducationalScreen
import com.example.strokefree.Screens.GameScreen
import com.example.strokefree.Screens.NewRiskAssessmentScreen
import com.example.strokefree.Screens.PastRiskAssessmentScreen
import com.example.strokefree.Screens.RiskAssessmentAnalysisScreen
import com.example.strokefree.Screens.RiskQuestionScreen
import com.example.strokefree.Screens.RiskScreen
import com.example.strokefree.ViewModels.EducationalViewModel
import com.example.strokefree.ViewModels.ExerciseTrackingViewModel
import com.example.strokefree.ViewModels.ExercisesViewModel
import com.example.strokefree.ViewModels.LoginViewModel
import com.example.strokefree.ViewModels.ForumViewModel
import com.example.strokefree.ViewModels.GoalCustomizationViewModel
import com.example.strokefree.ViewModels.MilestoneViewModel
import com.example.strokefree.ViewModels.OnnxViewModel
import com.example.strokefree.ViewModels.ProfileViewModel
import com.example.strokefree.ViewModels.SignUpViewModel
import com.example.strokefree.ViewModels.UserInfoViewModel
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val exerciseTrackingViewModel: ExerciseTrackingViewModel = viewModel()
    val exerciseViewModel: ExercisesViewModel = viewModel()
    val onnxViewModel: OnnxViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(navController = navController, viewModel = loginViewModel,)
        }

        composable("signUp") {
            val signUpViewModel: SignUpViewModel = viewModel()
            SignUpScreen(navController = navController, viewModel = signUpViewModel)
        }

        composable("userInfo") {
            val userInfoViewModel: UserInfoViewModel = viewModel()
            UserInfoScreen(navController = navController, viewModel = userInfoViewModel)
        }

        composable("base") {
            val educationalViewModel: EducationalViewModel = viewModel()
            val forumViewModel: ForumViewModel = viewModel()
            BaseScreen(
                navController = navController,
                educationalViewModel = educationalViewModel,
                exerciseViewModel = exerciseViewModel,
                forumViewModel = forumViewModel,
                profileViewModel = profileViewModel
            )
        }

        composable("forum") {
            val forumViewModel: ForumViewModel = viewModel()
            ForumScreen(navController = navController, viewModel = forumViewModel,  userID = "")
        }

        composable("createPost/{user_id}") { backStackEntry ->
            val userID = backStackEntry.arguments?.getString("user_id") ?: ""
            val forumViewModel: ForumViewModel = viewModel()
            NewPostScreen(navController = navController, viewModel = forumViewModel, userID = userID)
        }

        composable("postDetail/{post_id}/{user_id}/{username}") { backStackEntry ->
            val forumViewModel: ForumViewModel = viewModel()
            val postID = backStackEntry.arguments?.getString("post_id") ?: "UnknownPostID"
            val username = backStackEntry.arguments?.getString("username") ?: "Anonymous"
            val userID = backStackEntry.arguments?.getString("user_id") ?: "UnknownUserID"

            PostDetailScreen(
                navController,
                viewModel = forumViewModel,
                postID = postID,
                userID = userID,
                userName = username,
            )

        }


        composable("edit_profile/{user_id}") { backStackEntry ->
            val userID = backStackEntry.arguments?.getString("user_id") ?: ""
            EditProfileScreen(navController = navController, userID = userID)
        }

//        composable("notification/{user_id}") {
//            NotificationScreen(navController = navController)
//        }

        composable("progress_tracking/{user_id}") {
            val milestoneViewModel: MilestoneViewModel = viewModel()
            val goalCustomizationViewModel : GoalCustomizationViewModel = viewModel()
            ProgressTrackingScreen(userId = "", exerciseViewModel = exerciseViewModel, milestoneViewModel = milestoneViewModel, goalCustomizationViewModel = goalCustomizationViewModel, navController = navController)
        }
        composable("log_exercise/{user_id}") {
            LogExerciseScreen("", navController, exerciseTrackingViewModel, exerciseViewModel)
        }
        composable("goal_setting") {
            GoalSettingScreen(navController = navController)
        }
        composable("goal_customization/{isRecommended}") {
            val goalCustomizationViewModel: GoalCustomizationViewModel = viewModel()
            val isRecommended = it.arguments?.getString("isRecommended")
            GoalCustomizationScreen(
                navController,
                if (isRecommended == "recommended")
                    true else false,
                exercisesViewModel = exerciseViewModel,
                goalCustomizationViewModel = goalCustomizationViewModel
            )
        }

        composable("education") {
            val educationalViewModel: EducationalViewModel =
                viewModel() // ✅ Correct way to use ViewModel
            EducationalScreen(navController = navController, viewModel = educationalViewModel)
        }

        composable("contentDetail/{title}/{type}/{category}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Unknown"
            val type = backStackEntry.arguments?.getString("type") ?: "Unknown"
            val category = backStackEntry.arguments?.getString("category") ?: "Unknown"

            val educationalViewModel: EducationalViewModel = viewModel()
            val selectedContent = educationalViewModel.contentList.value.find { it.title == title }

            if (selectedContent != null) {
                ContentDetailScreen(navController, selectedContent)
            } else {
                Text("Error: Content Not Found", style = MaterialTheme.typography.bodyLarge)
            }
        }

        composable("risk") {
            RiskScreen(navController = navController)
        }

        composable("new_risk_assessment") {
            NewRiskAssessmentScreen(navController = navController)
        }

        composable("risk_question") {
//            val onnxViewModel: OnnxViewModel = viewModel()
            RiskQuestionScreen(navController = navController, onnxViewModel = onnxViewModel)
        }

        composable(
            route = "past_risk_assessment_screen/{assessmentId}",
            arguments = listOf(navArgument("assessmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assessmentId = backStackEntry.arguments?.getString("assessmentId") ?: "Unknown"
            PastRiskAssessmentScreen(navController = navController, assessmentId = assessmentId)
        }

        composable(route = "riskassessmentanalysis") {
            RiskAssessmentAnalysisScreen(navController = navController, onnxViewModel = onnxViewModel)
        }

        composable("game_screen") {
            GameScreen(navController)
        }




    }
}


