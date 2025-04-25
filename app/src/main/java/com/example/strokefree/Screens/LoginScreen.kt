package com.example.strokefree.Screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.view.Window
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.navigation.NavHostController
import com.example.strokefree.R
import com.example.strokefree.Repositories.UserRepo
import com.example.strokefree.StrokeFree
import com.example.strokefree.ViewModels.LoginViewModel
import com.example.strokefree.classes.NavigationEvent
import com.example.strokefree.ui.components.AnimatedWelcomeText
import com.example.strokefree.ui.components.CustomIconButton
import com.example.strokefree.ui.components.CustomTextField
import com.example.strokefree.ui.components.TextDivider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.math.roundToInt


@Composable
fun LoginScreen(navController: NavHostController, viewModel: LoginViewModel) {

    Box(modifier = Modifier.fillMaxSize()) {
        // Clickable area for animation when showWelcome is true
        if (viewModel.showWelcome.value) {
            WelcomeSection(viewModel)
        }
        // Full-screen Column when showWelcome is false
        if (!viewModel.showWelcome.value) {
            LoginSection(viewModel = viewModel, navController = navController)
        }
    }
}

@Composable
fun WelcomeSection(viewModel: LoginViewModel) {
    val scope = rememberCoroutineScope()
    //image animations
    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val screenHeightPixel = with(density) { screenHeight.toPx() }
    //text animation
    val textAlpha = remember { androidx.compose.animation.core.Animatable(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                scope.launch {
                    launch {
                        offsetY.animateTo(
                            targetValue = -screenHeightPixel / 2,
                            animationSpec = tween(durationMillis = 1000)
                        )
                    }
                    launch {
                        textAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 1000)
                        )
                    }
                    delay(1000)
                    viewModel.setWelcome(!viewModel.showWelcome.value)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .offset {
                    IntOffset(x = 0, y = offsetY.value.roundToInt())
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.strokepic),
                contentDescription = "StrokeFree",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(16.dp)
                .alpha(textAlpha.value),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedWelcomeText("Hello")
            AnimatedWelcomeText(
                "Welcome to StrokeFree",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                fontSize = 18,
                letterDelayMillis = 100
            )
        }
    }
}




@Composable
fun LoginSection(viewModel: LoginViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val isLandScape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val modifier = if (isLandScape) {
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    } else {
        Modifier.fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginTitle()
        Spacer(modifier = Modifier.weight(1f))
        LoginForm(viewModel, navController)
        Spacer(modifier = Modifier.weight(1f))
        //LoginFooter(viewModel, navController, context)
        LoginFooter(viewModel, navController)
    }
}


@Composable
fun LoginTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(100.dp))
        Text(
            "Login Here",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp
        )
        Text("Welcome to StrokeFree")
    }
}

@Composable
fun LoginForm(viewModel: LoginViewModel, navController: NavHostController) {
    val email by viewModel.email.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CustomTextField(
            value = email,
            onValueChange = { viewModel.setEmail(it) },
            label = "Email"
        )
        CustomTextField(
            value = viewModel.password.value,
            onValueChange = { viewModel.setPassword(it) },
            label = "Password",
            transformation = if (viewModel.passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            icon = if (viewModel.passwordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onClickIcon = {
                viewModel.togglePasswordVisibility()
            }
        )
        Button(
            onClick = {
                viewModel.login {
                    navController.navigate("base")
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(50.dp)
        ) {
            Text("Login")
        }
        Button(
            onClick = {
                navController.navigate("signUp")
            },
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(50.dp)
        ) {
            Text("Sign Up")
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.loading.value) {
                CircularProgressIndicator()
            }
            if (viewModel.error.value.isNotEmpty()) {
                Text(
                    text = viewModel.error.value,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


//@Composable
//fun LoginFooter(viewModel: LoginViewModel, navController: NavHostController, context: Context) {
//    val activity = context as? Activity
//
//    val googleSignInLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        try {
//            val account = task.getResult(ApiException::class.java)
//            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//
//            FirebaseAuth.getInstance().signInWithCredential(credential)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val firebaseUser = FirebaseAuth.getInstance().currentUser
//                        val userID = firebaseUser?.uid
//                        val db = FirebaseFirestore.getInstance()
//
//                        userID?.let {
//                            db.collection("users").document(it).get()
//                                .addOnSuccessListener { document ->
//                                    if (!document.exists()) {
//                                        UserRepo.createUserDocument(
//                                            userID = it,
//                                            name = firebaseUser.displayName ?: "",
//                                            email = firebaseUser.email ?: "",
//                                            onSuccess = { navController.navigate("userInfo") },
//                                            onFailure = { e -> Log.e("CreateUser", "Error", e) }
//                                        )
//                                    } else {
//                                        navController.navigate("base")
//                                    }
//                                }
//                        }
//                    } else {
//                        Log.e("GoogleSignIn", "Firebase Auth failed")
//                    }
//                }
//
//        } catch (e: ApiException) {
//            Log.e("GoogleSignIn", "Sign-in failed: ${e.statusCode}")
//        }
//    }
//
//
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        TextDivider()
//
//        Button(
//            onClick = {
//                viewModel.signInWithGoogle(
//                    context = context,
//                    onLoginSuccess = {
//                        navController.navigate("base")
//                    },
//                    onSignUpSuccess = {
//                        navController.navigate("userInfo")
//                    },
//                    fallback = { signInIntent ->
//                        googleSignInLauncher.launch(signInIntent)
//                    }
//                )
//            },
//            modifier = Modifier
//                .fillMaxWidth(0.65f)
//                .height(50.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.google),
//                    contentDescription = "Google",
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Sign in with Google")
//            }
//        }
//    }
//}



@Composable
fun LoginFooter(viewModel: LoginViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as StrokeFree

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    viewModel.handleLoginSuccess(firebaseUser)
                }
                .addOnFailureListener {
                    Log.e("GoogleSignIn", "Firebase Auth failed", it)
                }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Sign-in failed: ${e.statusCode}")
        }
    }
    // observe nav events
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                NavigationEvent.NavigateToUserInfo -> navController.navigate("userInfo")
                NavigationEvent.NavigateToBase -> navController.navigate("base")
            }
        }
    }

    Button(
        onClick = {
            // Start with CredentialManager
            signInWithCredentialManager(
                context = context,
                app = app,
                onCredentialSuccess = { idToken ->
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            viewModel.handleLoginSuccess(firebaseUser)
                        }
                        .addOnFailureListener {
                            Log.e("GoogleSignIn", "Firebase Auth failed", it)
                        }
                },
                fallback = { intent ->
                    googleSignInLauncher.launch(intent)
                }
            )
        },
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .height(50.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google")
        }
    }
}

fun signInWithCredentialManager(
    context: Context,
    app: StrokeFree,
    onCredentialSuccess: (String) -> Unit,
    fallback: (Intent) -> Unit = {}
) {
    FirebaseAuth.getInstance().signOut()
    val credentialManager = CredentialManager.create(context)

    val googleIDOptions = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(app.webClientID)
        .setAutoSelectEnabled(false)
        .setNonce("123456")
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIDOptions)
        .build()

    CoroutineScope(Dispatchers.Main).launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                onCredentialSuccess(idToken)
            } else {
                Log.e("CredentialManager", "Invalid credential type")
            }
        } catch (e: Exception) {
            Log.e("CredentialManager", "Error: ${e.message}, falling back to Intent")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(app.webClientID)
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(context, gso)
            fallback(client.signInIntent)
        }
    }
}