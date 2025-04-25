package com.example.strokefree.Screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.EducationalContent

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ContentDetailScreen(navController: NavController, content: EducationalContent) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // ✅ Automatically open YouTube if it's a video
    LaunchedEffect(content.type) {
        if (content.type == "Video") {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(content.url)).apply {
                `package` = "com.google.android.youtube" // Open in YouTube app
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback: Open in browser if YouTube app is not installed
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(content.url)))
            }
            navController.popBackStack() // Close details screen after opening YouTube
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ✅ Top App Bar
        TopAppBar(
            title = { Text(content.title) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        // ✅ Keep WebView for Articles & Journals
        if (content.type == "Article" || content.type == "Journal") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Source: ${content.url}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                WebViewContainer(url = content.url)
            }
        }
    }
}

// ✅ WebView Component (For Articles & Journals)
@Composable
fun WebViewContainer(url: String) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
