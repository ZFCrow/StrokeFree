package com.example.strokefree.classes

import java.time.LocalDateTime

data class Notification(
    val title: String,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val type: Type // Based on the type, it can navigate to different pages on click
) {
    enum class Type { Home, Risk, Forum, Profile, Educational }
}