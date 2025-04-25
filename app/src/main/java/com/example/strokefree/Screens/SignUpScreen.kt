package com.example.strokefree.Screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.SignUpViewModel
import com.example.strokefree.ui.components.CustomIconButton
import com.example.strokefree.ui.components.CustomTextField


@Composable
fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val modifier = if (isLandscape) {
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    } else {
        Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignUpHeader()
        Spacer(modifier = Modifier.weight(1f))
        SignUpForm(viewModel, navController)
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
fun SignUpHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            "Create an Account",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp
        )
        Text("Welcome to StrokeFree")
    }
}


@Composable
fun SignUpForm(viewModel: SignUpViewModel, navController: NavController) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomTextField(
            value = viewModel.name.value,
            onValueChange = { viewModel.setName(it) },
            label = "Name"
        )
        CustomTextField(
            value = viewModel.email.value,
            onValueChange = { viewModel.setEmail(it) },
            label = "Email"
        )
        CustomTextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.setPassword(it) },
            label = "Password",
            transformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onClickIcon = {
                passwordVisible = !passwordVisible
            }
        )
        CustomTextField(
            value = viewModel.confirmPassword.value,
            onValueChange = { viewModel.setConfirmPassword(it) },
            label = "Confirm Password",
            transformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            icon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onClickIcon = {
                confirmPasswordVisible = !confirmPasswordVisible
            }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = viewModel.termsAgreement.value,
                onCheckedChange = { viewModel.setTermsAgreement(it) }
            )
            Text("I agree to the terms and conditions")
        }
        Button(
            onClick = {
                viewModel.createAccount {
                    navController.navigate("userInfo")
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(50.dp),
            enabled = viewModel.email.value.isNotEmpty() &&
                    viewModel.password.value.isNotEmpty() &&
                    viewModel.name.value.isNotEmpty() &&
                    viewModel.confirmPassword.value.isNotEmpty() &&
                    viewModel.termsAgreement.value
        ) {
            Text("Create Account")
        }

        if (viewModel.error.value.isNotEmpty()) {
            Text(
                text = viewModel.error.value,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (viewModel.loading.value) {
            CircularProgressIndicator()
        }
    }
}


//@Composable
//fun SignUpScreen(navController: NavController, viewModel: SignUpViewModel){
//    var passwordVisible by remember { mutableStateOf(false) }
//    var confirmPasswordVisible by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(WindowInsets.systemBars.asPaddingValues())
//    )
//    {
//        // header
//        Column(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ){
//            Spacer(modifier = Modifier
//                .height(100.dp))
//
//            Text("Create an Account",
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                fontSize = 36.sp)
//            Text ("Welcome to StrokeFree")
//        }
//
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        //Textfields
//        Column (
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ){
//            CustomTextField(
//                value = viewModel.name.value,
//                onValueChange = { viewModel.setName(it) },
//                label = "Name"
//            )
//            CustomTextField(
//                value = viewModel.email.value,
//                onValueChange = { viewModel.setEmail(it) },
//                label = "Email"
//            )
//            CustomTextField(
//                value = viewModel.password.value,
//                onValueChange = { viewModel.setPassword(it) },
//                label = "Password",
//                transformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                onClickIcon = {
//                    passwordVisible = !passwordVisible
//
//                }
//            )
//            CustomTextField(
//                value = viewModel.confirmPassword.value,
//                onValueChange = { viewModel.setConfirmPassword(it) },
//                label = "Confirm Password",
//                transformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                icon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
//                onClickIcon = {
//                    confirmPasswordVisible = !confirmPasswordVisible
//
//                }
//            )
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ){
//                Checkbox(
//                    checked = viewModel.termsAgreement.value,
//                    onCheckedChange = { viewModel.setTermsAgreement(it) }
//                )
//                Text ("I agree to the terms and conditions")
//            }
//            Button(
//                onClick = {
//                    viewModel.createAccount {
//                        navController.navigate("userInfo")
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth(0.65f)
//                    .height(50.dp),
//                enabled = viewModel.email.value.isNotEmpty() &&
//                        viewModel.password.value.isNotEmpty() &&
//                        viewModel.name.value.isNotEmpty() &&
//                        viewModel.confirmPassword.value.isNotEmpty() &&
//                        viewModel.termsAgreement.value
//
//            ) {
//                Text ("Create Account")
//            }
//
//            if (viewModel.error.value.isNotEmpty()){
//                Text(
//                    text = viewModel.error.value,
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
//
//            if (viewModel.loading.value){
//                CircularProgressIndicator()
//            }
//
//        }
//
//
//        Spacer(modifier = Modifier.weight(1f))
//
//    }
//}

