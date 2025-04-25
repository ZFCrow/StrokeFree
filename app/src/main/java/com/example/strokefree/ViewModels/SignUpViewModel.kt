package com.example.strokefree.ViewModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.strokefree.Repositories.UserRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class SignUpViewModel: ViewModel() {
    private val _name = mutableStateOf("")
    var name: State<String> = _name
    private val _email = mutableStateOf("")
    var email: State<String> = _email

    private val _password = mutableStateOf("")
    var password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    var confirmPassword: State<String> = _confirmPassword

    private val _termsAgreement = mutableStateOf(false)
    var termsAgreement: State<Boolean> = _termsAgreement

    private val _loading = mutableStateOf(false)
    var loading: State<Boolean> = _loading

    private val _error = mutableStateOf("")
    var error: State<String> = _error

    fun setName(name: String) {
        _name.value = name
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setConfirmPassword(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
    }

    fun setTermsAgreement(termsAgreement: Boolean) {
        _termsAgreement.value = termsAgreement
    }

    fun createAccount(onSuccess: () -> Unit) {
        if (_name.value.isEmpty() || _email.value.isEmpty() || _password.value.isEmpty() || _confirmPassword.value.isEmpty()) {
            _error.value = "Please fill in all fields"
            return
        }
        if (_password.value != _confirmPassword.value) {
            _error.value = "Passwords do not match"
            return
        }
        _loading.value = true

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(_email.value, _password.value)
            .addOnSuccessListener { authResult ->
                val userID = authResult.user?.uid ?: ""

                UserRepo.createUserDocument(
                    userID = userID,
                    name = _name.value,
                    email = _email.value,
                    onSuccess = onSuccess,
                    onFailure = { e ->
                        onErrorMessageShown(e)
                    }
                )

            }
            .addOnFailureListener{e->
                onErrorMessageShown(e)
            }

    }

    fun onErrorMessageShown(error: Exception) {
        _loading.value = false
        _error.value = error.message.toString()
    }
}