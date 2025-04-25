package com.example.strokefree.classes

// NavigationEvent.kt
sealed class NavigationEvent {
    object NavigateToUserInfo : NavigationEvent()
    object NavigateToBase : NavigationEvent()
}
