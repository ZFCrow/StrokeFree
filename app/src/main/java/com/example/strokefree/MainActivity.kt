package com.example.strokefree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.strokefree.ui.theme.StrokeFreeTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.strokefree.Screens.HomeScreen
import com.example.strokefree.Screens.SignUpScreen
import com.example.strokefree.Screens.LoginScreen
import com.example.strokefree.Screens.UserInfoScreen
import com.example.strokefree.ViewModels.LoginViewModel
import com.example.strokefree.ViewModels.SignUpViewModel
import com.example.strokefree.ViewModels.UserInfoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StrokeFreeTheme {
                NavGraph()
                }
            }
        }
    }

