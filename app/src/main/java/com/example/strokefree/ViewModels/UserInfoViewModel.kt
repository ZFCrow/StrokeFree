package com.example.strokefree.ViewModels

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UserInfoViewModel: ViewModel() {
    private val _DOB = mutableStateOf("")
    var DOB = _DOB

    private val _gender = mutableStateOf("")
    var gender = _gender

    private val _phoneNumber = mutableStateOf("")
    var phoneNumber = _phoneNumber

    private val _bloodType = mutableStateOf("")
    var bloodType = _bloodType

    private val _isDiagonised = mutableStateOf(false)
    var isDiagonised = _isDiagonised

    private val _strokeType = mutableStateOf("")
    var strokeType = _strokeType

    //list of medical conditions
    private val _selectedConditions = mutableStateListOf<String>()
    var selectedConditions: List<String> = _selectedConditions

    private val _confirmation = mutableStateOf(false)
    var confirmation = _confirmation

    private val _loading = mutableStateOf(false)
    var loading = _loading

    private val _error = mutableStateOf("")
    var error = _error


    private val _showDatePicker = mutableStateOf(false)
    var showDatePicker = _showDatePicker

    var medConList =
        listOf<String>("Diabetes", "Hypertension", "Heart Disease", "Cancer", "Asthma", "Others")
    var bloodTypeList = listOf<String>("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    var strokeTypeList = listOf<String>("Ischemic", "Hemorrhagic","Transient Ischemic Attack(TIA)", "Cerebellar","Brain Stem", "Others")


    fun toggleDatePicker(){
        _showDatePicker.value = !_showDatePicker.value
    }


    fun setDOB(DOB: String){
        _DOB.value = DOB
    }

    fun setGender (gender: String){
        _gender.value = gender
    }
    fun setPhoneNumber(phoneNumber : String){
        _phoneNumber.value = phoneNumber
    }
    fun setBloodType(bloodType: String){
        _bloodType.value = bloodType
    }
    fun setStrokeType(strokeType: String){
        _strokeType.value = strokeType
    }
    fun setIsDiagonised(isDiagonised: Boolean){
        _isDiagonised.value = isDiagonised
    }

    fun addCondition(condition: String){
        _selectedConditions.add(condition)
    }
    fun removeCondition(condition: String){
        _selectedConditions.remove(condition)
    }
    fun setConfirmation(confirmation: Boolean){
        _confirmation.value = confirmation
    }


    fun updateUserInfo(profilePicBase64: String = "",onSuccess: ()-> Unit){
        val phoneRegex = Regex("^[689]\\d{7}$")
        _error.value = "" //clear error text if any
        if (_phoneNumber.value == "" || _bloodType.value == "" || _DOB.value == ""){
            _error.value = "Please fill in all fields"
            return
        }
        if (!phoneRegex.matches(_phoneNumber.value)) {
            _error.value = "Invalid Singapore phone number"
            return
        }
        if (_isDiagonised.value && _strokeType.value == ""){
            _error.value = "Please select a stroke type"
            return
        }

        //update user info in database
        // get current date in yyyy-mm-dd format
        val todaysDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        // look for the user in the database with the doc id of userid and update the fields
        if (userID != null){
            val userInfo = mapOf(
                "DOB" to _DOB.value,
                "gender" to if (_gender.value.isEmpty()) "Non-Binary" else _gender.value,
                "phoneNumber" to _phoneNumber.value,
                "bloodType" to _bloodType.value,
                "medicalConditions" to _selectedConditions.toList(),
                "isDiagnosed" to _isDiagonised.value,
                "strokeType" to if (_isDiagonised.value) _strokeType.value else "",
                "userGoals" to emptyList<Map<String, Any>>(),
                // a map where the key is the date and the value is a list of logs
                "userLogs" to mapOf(todaysDate to emptyMap<String,Any>()),
                "imageURL" to profilePicBase64
            )
            db.collection("users")
                .document(userID)
                .update(userInfo)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    _error.value = e.message ?: "An unknown error occurred"
                }
        } else {
            _error.value = "User not found, Please Log in again"
        }
    }

}