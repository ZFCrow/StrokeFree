package com.example.strokefree.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.util.*
import android.util.Base64
import androidx.compose.material.icons.filled.Person


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    userID: String
) {
    val userProfile by profileViewModel.userProfile

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var dob by remember { mutableStateOf(TextFieldValue("")) }
    var gender by remember { mutableStateOf("Male") }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var medicalConditions by remember { mutableStateOf(TextFieldValue("")) }

    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile(userID)
    }

    // Fetch user data
    LaunchedEffect(userProfile) {
        if (userProfile != null && !isInitialized) {
            name = TextFieldValue(userProfile!!.name)
            dob = TextFieldValue(userProfile!!.dob)
            gender = userProfile!!.gender
            email = TextFieldValue(userProfile!!.email)
            phoneNumber = TextFieldValue(userProfile!!.phoneNumber)
            medicalConditions = TextFieldValue(userProfile!!.medicalConditions.joinToString(", "))
            isInitialized = true
        }
    }

    fun validateInputs(): Boolean {
        var isValid = true

        if (name.text.isBlank()) {
            nameError = "Full Name is required"
            isValid = false
        } else {
            nameError = null
        }

        if (dob.text.isBlank()) {
            dobError = "Date of Birth is required"
            isValid = false
        } else {
            dobError = null
        }

        if (email.text.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
            emailError = "Enter a valid email"
            isValid = false
        } else {
            emailError = null
        }

        if (phoneNumber.text.isBlank() || !phoneNumber.text.matches(Regex("\\d{8,15}"))) {
            phoneError = "Enter a valid phone number"
            isValid = false
        } else {
            phoneError = null
        }

        return isValid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProfilePictureSection(profileViewModel = profileViewModel, userID)
            }
            item {
                ProfileEditForm(
                    name = name,
                    dob = dob,
                    gender = gender,
                    email = email,
                    phoneNumber = phoneNumber,
                    medicalConditions = medicalConditions,
                    onNameChange = { name = it },
                    onDobChange = { dob = it },
                    onGenderChange = { gender = it },
                    onEmailChange = { email = it },
                    onPhoneChange = { phoneNumber = it },
                    onMedicalConditionsChange = { medicalConditions = it },
                    nameError = nameError,
                    dobError = dobError,
                    emailError = emailError,
                    phoneError = phoneError,
                    onSave = {
                        if (validateInputs()) {
                            profileViewModel.updateUserProfile(
                                userID,
                                name.text,
                                dob.text,
                                gender,
                                email.text,
                                phoneNumber.text
                            )

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("profileUpdated", true)

                            navController.popBackStack()
                        }

                    }
                )
            }
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
fun ProfilePictureSection(profileViewModel: ProfileViewModel, userID: String) {
    val context = LocalContext.current
    val userProfile by profileViewModel.userProfile

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileViewModel.uploadProfileImageBase64(context, userID, uri) { success ->
                if (!success) {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
                // No need to manually trigger state here – fetchUserProfile already updates LiveData
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            val bitmap = try {
                val encodedImage = userProfile?.imageURL
                if (!encodedImage.isNullOrBlank()) {
                    val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else null
            } catch (e: Exception) {
                null
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        IconButton(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Picture")
        }
    }
}




@Composable
fun ProfileEditForm(
    name: TextFieldValue,
    dob: TextFieldValue,
    gender: String,
    email: TextFieldValue,
    phoneNumber: TextFieldValue,
    medicalConditions: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    onDobChange: (TextFieldValue) -> Unit,
    onGenderChange: (String) -> Unit,
    onEmailChange: (TextFieldValue) -> Unit,
    onPhoneChange: (TextFieldValue) -> Unit,
    onMedicalConditionsChange: (TextFieldValue) -> Unit,
    nameError: String?,
    dobError: String?,
    emailError: String?,
    phoneError: String?,
    onSave: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        SectionTitle("Basic Details")
        EditProfileTextField(name, "Full Name", onValueChange = onNameChange, error = validateName(name))
        //EditProfileTextField(dob, "Date of Birth (dd-MM-YYYY)", onValueChange = onDobChange, error = validateDOB(dob))
        DOBPickerField(dob = dob, onDobChange = onDobChange, error = validateDOB(dob))

        Text("Gender", fontSize = 16.sp, fontWeight = MaterialTheme.typography.bodyLarge.fontWeight)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenderOption("Male", gender, onGenderChange)
            GenderOption("Female", gender, onGenderChange)
            GenderOption("Non-Binary", gender, onGenderChange)
        }

        SectionTitle("Contact Details")
        EditProfileTextField(email, "Email", onValueChange = onEmailChange, error = validateEmail(email))
        EditProfileTextField(phoneNumber, "Phone Number", onValueChange = onPhoneChange, error = validatePhone(phoneNumber))

        SectionTitle("Medical Details")
        EditProfileTextField(medicalConditions, "Medical Conditions (comma-separated)", onValueChange = onMedicalConditionsChange)

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = nameError == null && dobError == null && emailError == null && phoneError == null
        ) {
            Text("Save Changes", fontSize = 18.sp)
        }
    }
}

@Composable
fun EditProfileTextField(
    value: TextFieldValue,
    label: String,
    onValueChange: (TextFieldValue) -> Unit,
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}


@Composable
fun GenderOption(option: String, selectedGender: String, onSelected: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        RadioButton(
            selected = selectedGender == option,
            onClick = { onSelected(option) }
        )
        Text(option)
    }
}

fun validateName(value: TextFieldValue): String? {
    return if (value.text.isBlank()) "Full Name is required" else null
}

fun validateDOB(value: TextFieldValue): String? {
    if (value.text.isBlank()) return "Date of Birth is required"

    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val enteredDate = LocalDate.parse(value.text, formatter)
        val today = LocalDate.now()

        if (enteredDate.isAfter(today)) {
            "Date of Birth cannot be in the future"
        } else null
    } catch (e: DateTimeParseException) {
        "Invalid date format (expected dd/MM/yyyy)"
    }
}




fun validateEmail(value: TextFieldValue): String? {
    return if (value.text.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(value.text).matches()) {
        "Enter a valid email"
    } else null
}

fun validatePhone(value: TextFieldValue): String? {
    return if (value.text.isBlank() || !value.text.matches(Regex("\\d{8,15}"))) {
        "Enter a valid phone number"
    } else null
}

@Composable
fun DOBPickerField(
    dob: TextFieldValue,
    onDobChange: (TextFieldValue) -> Unit,
    error: String?
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy") // match stored format

    // Try to parse existing DOB to show in picker
    val initialDate = try {
        LocalDate.parse(dob.text, formatter)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val year = initialDate.year
    val month = initialDate.monthValue - 1 // Calendar months are 0-based
    val day = initialDate.dayOfMonth

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            val formattedDate = selectedDate.format(formatter)
            onDobChange(TextFieldValue(formattedDate)) // ✅ this is where it goes
        },
        year,
        month,
        day
    )

    // Prevent selecting future dates
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    Column {
        OutlinedTextField(
            value = dob,
            onValueChange = {}, // no manual editing
            label = { Text("Date of Birth (dd/MM/yyyy)") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    datePickerDialog.show()
                },
            trailingIcon = {
                IconButton(onClick = {
                    datePickerDialog.show()
                }) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Pick Date")
                }
            },
            isError = error != null
        )


        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }
}





