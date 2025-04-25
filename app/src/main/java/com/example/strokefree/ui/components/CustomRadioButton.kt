package com.example.strokefree.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun CustomRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    text: String
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
    ){
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text)
    }
}