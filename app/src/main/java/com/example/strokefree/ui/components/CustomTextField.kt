package com.example.strokefree.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    transformation : VisualTransformation = VisualTransformation.None,
    icon : ImageVector? = null,
    onClickIcon : () -> Unit = {},
    keyboardOptions : KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
    readOnly : Boolean = false
){
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth(0.65f),
        visualTransformation = transformation,

        trailingIcon = {
            icon?.let {
                IconButton(
                    onClick = onClickIcon
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "icon",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        readOnly = readOnly
    )
}