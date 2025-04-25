package com.example.strokefree.Screens

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Base64
import android.util.Log
import android.widget.RadioButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.strokefree.ViewModels.UserInfoViewModel
import com.example.strokefree.ui.components.CustomIconButton
import com.example.strokefree.ui.components.CustomTextField
import java.text.SimpleDateFormat
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.strokefree.R
import com.example.strokefree.ui.components.CustomDropdown
import com.example.strokefree.ui.components.CustomRadioButton
import org.intellij.lang.annotations.JdkConstants.VerticalScrollBarPolicy
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(navController: NavController, viewModel: UserInfoViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val context = LocalContext.current
    fun drawableToBase64(drawableId: Int): String {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    // datepicker
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        // only allow presnet and past , not future
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
    if (viewModel.showDatePicker.value) {
        Dialog (onDismissRequest = { viewModel.toggleDatePicker() }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isLandscape) Modifier
                                .heightIn(max = configuration.screenHeightDp.dp * 0.85f)
                                .verticalScroll(rememberScrollState())
                            else Modifier.wrapContentHeight()
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DatePicker(state = datePickerState)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.toggleDatePicker() }) {
                            Text("Cancel")
                        }
                        TextButton(onClick = {
                            val selectedDate = datePickerState.selectedDateMillis
                            selectedDate?.let {
                                val date = java.util.Date(it)
                                val format = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                viewModel.setDOB(format.format(date))
                            }
                            viewModel.toggleDatePicker()
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        LazyColumn (
            modifier = Modifier
                .weight(1f)
        )
        {
            item(){
                // header
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(
                        modifier = Modifier
                            .height(70.dp)
                    )

                    Text(
                        "Tell us more about yourself",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    )
                    Text("Welcome to StrokeFree")
                }
            }

            item{
                //Textfields
                Column(
                    modifier = Modifier
                        .fillMaxWidth(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GenderSection(viewModel = viewModel)
                    DOBSection(viewModel = viewModel)
                    PhoneNumberSection(viewModel = viewModel)
                    BloodTypeSection(viewModel = viewModel)
                    DiagnosedSection(viewModel = viewModel)
                    ExistingMedicalConditionSection(viewModel = viewModel)
                    TermsAndConditionsAgreementSection(viewModel = viewModel)


                }
            }
        }
        if (viewModel.error.value.isNotEmpty()){
            Text(
                text = viewModel.error.value,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Button(
            onClick = {
                val imageResource = when (viewModel.gender.value.lowercase(java.util.Locale.getDefault())) {
                    "male" -> R.drawable.male
                    "female" -> R.drawable.female
                    else -> R.drawable.nb
                }
                val profileImageBase64 = drawableToBase64(imageResource)
                viewModel.updateUserInfo (profileImageBase64){
                    navController.navigate("base")
                }
            },
            enabled = viewModel.DOB.value != "" && viewModel.phoneNumber.value != "" && viewModel.bloodType.value != "" && viewModel.confirmation.value,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(50.dp)

        ) {
            Text("Next")
        }
    }
}

@Composable
fun GenderSection(viewModel: UserInfoViewModel){
    Row {
        CustomIconButton(
            onclickDoSomething = {
                viewModel.setGender("Male")
            },
            icon = Icons.Filled.Male,
            contentDescription = "Male",
            iconTint = if (viewModel.gender.value == "Male") Color.Blue else Color.Black,
            iconSize = 80
        )
        CustomIconButton(
            onclickDoSomething = {
                viewModel.setGender("Female")
            },
            icon = Icons.Filled.Female,
            contentDescription = "Female",
            iconTint = if (viewModel.gender.value == "Female") Color.Red else Color.Black,
            iconSize = 80
        )
    }
}
@Composable
fun DOBSection (viewModel: UserInfoViewModel){
    CustomTextField(
        value = viewModel.DOB.value,
        onValueChange = { },
        label = "DOB",
        icon = Icons.Default.CalendarToday,
        onClickIcon = {
            viewModel.toggleDatePicker()
        },
        readOnly = true
    )
}

@Composable
fun PhoneNumberSection(viewModel: UserInfoViewModel){
    CustomTextField(
        value = viewModel.phoneNumber.value,
        onValueChange = { number ->
            val filteredNumber = number.filter { it.isDigit() }
            viewModel.setPhoneNumber(filteredNumber)
                        },
        label = "Phone Number",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
}
@Composable
fun BloodTypeSection(viewModel: UserInfoViewModel){
    CustomDropdown (
        list = viewModel.bloodTypeList,
        selected = viewModel.bloodType.value,
        label = "Blood Type"
    ) { bloodType ->
        viewModel.setBloodType(bloodType)
    }
}

@Composable
fun DiagnosedSection(viewModel: UserInfoViewModel){
    Column {
        Text("Are you diagonised with stroke?")
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            //radio button
            CustomRadioButton(
                selected = viewModel.isDiagonised.value,
                onClick = { viewModel.setIsDiagonised(true) },
                text = "Yes"
            )
            CustomRadioButton(
                selected = !viewModel.isDiagonised.value,
                onClick = { viewModel.setIsDiagonised(false) },
                text = "No"
            )
        }
        if (viewModel.isDiagonised.value){
            CustomDropdown(
                list = viewModel.strokeTypeList,
                selected = viewModel.strokeType.value,
                label = "Stroke Type"
            ) { strokeType ->
                viewModel.setStrokeType(strokeType)
            }
        }
    }
}

@Composable
fun ExistingMedicalConditionSection(viewModel: UserInfoViewModel) {
    val listState = rememberLazyListState() // Track scroll state

    Row( // Use Row to place list + scrollbar
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth(0.65f)
    ) {
        // ✅ Main Column (List + Title)
        Column(modifier = Modifier.weight(1f)) {
            Text("Existing Medical Conditions")
            LazyColumn(
                state = listState, // Attach scroll state
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.medConList) { cond ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.selectedConditions.contains(cond),
                            onCheckedChange = {
                                if (viewModel.selectedConditions.contains(cond)) {
                                    viewModel.removeCondition(cond)
                                } else {
                                    viewModel.addCondition(cond)
                                }
                            }
                        )
                        Text(cond)
                    }
                }
            }
        }

        // ✅ Custom Scrollbar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(8.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
        ) {

            val totalScrollableItems = maxOf(viewModel.medConList.size - listState.layoutInfo.visibleItemsInfo.size, 1)

            // ✅ Scroll Offset (Fixing Division by Zero)
            val scrollOffset = (listState.firstVisibleItemScrollOffset.toFloat() /
                    ((listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.toFloat() ?: 1f)))
                .coerceIn(0f, 1f) // ✅ Ensure it's always between 0-1

            val targetPosition  = (
                    listState.firstVisibleItemIndex.toFloat() +
                            scrollOffset
                    ) / totalScrollableItems

            val animatedPosition by animateFloatAsState(
                targetValue = targetPosition,
                animationSpec = tween(durationMillis = 150, easing = LinearEasing)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp) // Scrollbar size
                    .align(Alignment.TopStart)
                    .offset(y = (animatedPosition * 160f).dp)
                    .background(Color.DarkGray, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}



@Composable
fun TermsAndConditionsAgreementSection(viewModel: UserInfoViewModel){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(0.65f)
    ) {
        Checkbox(
            checked = viewModel.confirmation.value,
            onCheckedChange = { viewModel.setConfirmation(it) }
        )
        Text("I confirm that the information provided is accurate and complete to the best of my knowledge")
    }
}




@Preview (showBackground = true)
@Composable
fun UserinfoScreenPreview()
{
    UserInfoScreen(navController = rememberNavController(), viewModel = UserInfoViewModel())
}