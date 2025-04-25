package com.example.strokefree.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimatedWelcomeText(
    fullText: String,
    letterDelayMillis: Long = 300,
    style : TextStyle = MaterialTheme.typography.titleLarge,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: Int = 36,
) {
    var animatedText by remember { mutableStateOf("") }

    // This LaunchedEffect will run once when the composable enters the composition.
    LaunchedEffect (key1 = fullText) {
        // Append one letter at a time
        for (char in fullText) {
            animatedText += char
            delay(letterDelayMillis)
        }
    }

    Text(
        text = animatedText,
        style = style,
        fontWeight = fontWeight,
        fontSize = fontSize.sp
    )
}