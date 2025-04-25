package com.example.strokefree.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.strokefree.ViewModels.ForumViewModel
import com.example.strokefree.classes.Post
import com.example.strokefree.classes.PostStatus
import com.example.strokefree.classes.PostVisibility
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreen(
    navController: NavController,
    viewModel: ForumViewModel = viewModel(),
    userID: String
) {

    Log.d("NewPostScreen", "User ID: $userID")

    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(listOf("General Discussion")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val authorName by viewModel.authorName.collectAsState()
    var visibility by remember { mutableStateOf(PostVisibility.PUBLIC) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Post Title
            Text("Post Title", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Post Title") },
                placeholder = { Text("Enter your post title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category
            Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            MultiSelectDropdown(
                selectedCategories = selectedCategories,
                onCategorySelected = { selectedCategories = it }
            )

            // Post Content (Stretchable)
            Text("Post Content", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                placeholder = { Text("Write your post here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // <--- stretches to fill space
                shape = RoundedCornerShape(12.dp)
            )

            // Visibility
            Text("Post Visibility", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = visibility == PostVisibility.PUBLIC,
                    onClick = { visibility = PostVisibility.PUBLIC },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Text("Public")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = visibility == PostVisibility.PRIVATE,
                    onClick = { visibility = PostVisibility.PRIVATE },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Text("Private")
            }

            // Create Button (now at the bottom)
            Button(
                onClick = {
                    coroutineScope.launch {
                        val newPost = Post(
                            id = "",
                            authorId = userID,
                            authorName = authorName,
                            title = title,
                            content = content,
                            categories = selectedCategories,
                            imageUrls = listOfNotNull(imageUri?.toString()),
                            timestamp = System.currentTimeMillis(),
                            visibility = visibility,
                            status = PostStatus.ACTIVE
                        )
                        viewModel.createPost(newPost) { success ->
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Create", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

}



@Composable
fun MultiSelectDropdown(
    selectedCategories: List<String>,
    onCategorySelected: (List<String>) -> Unit
) {
    val categories = listOf(
        "General Discussion",
        "Symptoms & Recovery",
        "Treatment & Medications",
        "Rehabilitation & Exercises",
        "Diet & Lifestyle",
        "Support & Personal Stories"
    )

    var expanded by remember { mutableStateOf(false) }

    Box {
        // Button to open the dropdown
        TextButton(onClick = { expanded = true }) {
            Text(if (selectedCategories.isNotEmpty()) selectedCategories.joinToString(", ") else "Select Categories")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { category ->
                val isSelected = category in selectedCategories
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        val updatedCategories = if (isSelected) {
                            selectedCategories - category // Remove category if selected
                        } else {
                            selectedCategories + category // Add category if not selected
                        }
                        onCategorySelected(updatedCategories) // ✅ Pass updated list
                    },
                    leadingIcon = {
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Create Post Screen Preview")
@Composable
fun PreviewCreatePostScreen() {
    NewPostScreen(navController = rememberNavController(), userID = "")
}
