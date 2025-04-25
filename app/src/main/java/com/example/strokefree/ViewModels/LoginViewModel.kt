package com.example.strokefree.ViewModels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.strokefree.Repositories.UserRepo
import com.example.strokefree.StrokeFree
import com.example.strokefree.classes.NavigationEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirestoreRegistrar
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    private val _showWelcome = mutableStateOf(true)
    val showWelcome: State<Boolean> = _showWelcome

//    private val _email = mutableStateOf("")
//    val email: State<String> = _email
    
    // lets try mutablestateflow
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible : State<Boolean> = _passwordVisible

    fun togglePasswordVisibility(){
        _passwordVisible.value = !_passwordVisible.value
    }

    private val _autoLogIn = mutableStateOf(false)
    val autoLogIn: State<Boolean> = _autoLogIn

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun setWelcome(showWelcome: Boolean){
        _showWelcome.value = showWelcome
    }
    fun setEmail(email: String){
        _email.value = email
    }
    fun setPassword(password: String){
        _password.value = password
    }

    fun setAutoLogIn(autoLogIn: Boolean){
        _autoLogIn.value = autoLogIn
    }

    fun login(onSuccess: () -> Unit){
        //clear error text if any
        _error.value = ""
        //login user
        if (_email.value.isEmpty() || _password.value.isEmpty()){
            _error.value = "Please fill in all fields"
            return
        }
        _loading.value = true

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(_email.value, _password.value)
            .addOnSuccessListener {
                _loading.value = false
                onSuccess()
            }
            .addOnFailureListener(){ exception ->

                _loading.value = false
                _error.value = when(exception) {
                    is FirebaseAuthInvalidUserException -> "User does not exist"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
                    else -> exception.message ?: "Login failed"
                }
            }
    }

    

    fun handleLoginSuccess(user: FirebaseUser?) {
        val userId = user?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                viewModelScope.launch {
                    if (!doc.exists()) {
                        UserRepo.createUserDocument(
                            userID = userId,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            onSuccess = {
                                viewModelScope.launch {
                                    _navigationEvent.emit(NavigationEvent.NavigateToUserInfo)
                                }
                            },
                            onFailure = {
                                Log.e("Firestore", "User creation failed", it)
                            }
                        )
                    } else {
                        _navigationEvent.emit(NavigationEvent.NavigateToBase)
                    }
                }
            }
            .addOnFailureListener {
                        Log.e("Firestore", "Failed to check user doc", it)
            }
    }

    }



