package com.example.strokefree.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.strokefree.ViewModels.ForumViewModel
import com.example.strokefree.classes.Post
import com.example.strokefree.classes.PostMetrics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ForumScreen(
    navController: NavController,
    viewModel: ForumViewModel = viewModel(),
    userID: String
) {
    val userID by remember { mutableStateOf((viewModel.userID)) } // get UID

    //var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    //var postMetrics by remember { mutableStateOf<Map<String, PostMetrics>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf("All Time") }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    val searchResults by viewModel.searchResults.collectAsState()
    val postMetrics by viewModel.postMetrics.collectAsState()
    val posts by viewModel.posts.collectAsState()

    // Fetch posts and their metrics from Firestore
    LaunchedEffect(selectedDateRange, selectedCategories) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null // Reset error state

            if (selectedDateRange == "All Time" && selectedCategories.isEmpty()) {
                viewModel.getAllActivePosts(
                    onSuccess = { fetchedPosts ->
                        viewModel.updatePosts(fetchedPosts)

                        val postIds = fetchedPosts.map { it.id }
                        viewModel.getAllPostMetrics(
                            postIds = postIds,
                            onSuccess = { metricsList ->
                                val metricsMap = metricsList.associateBy { it.postId }
                                viewModel.updatePostMetrics(metricsMap)
                            },
                            onError = { error -> errorMessage = error }
                        )
                    },
                    onError = { error -> errorMessage = error }
                )
            } else {
                viewModel.getFilteredPostsByDateAndCategory(
                    dateRange = selectedDateRange,
                    selectedCategories = selectedCategories,
                    onSuccess = { fetchedPosts ->
                        viewModel.updatePosts(fetchedPosts)

                        val postIds = fetchedPosts.map { it.id }
                        viewModel.getAllPostMetrics(
                            postIds = postIds,
                            onSuccess = { metricsList ->
                                val metricsMap = metricsList.associateBy { it.postId }
                                viewModel.updatePostMetrics(metricsMap)
                            },
                            onError = { error -> errorMessage = error }
                        )
                    },
                    onError = { error -> errorMessage = error }
                )
            }

            isLoading = false
        }
    }


    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        // Search Bar + Filter + Add Post Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    if (newValue.text.isNotEmpty()) {
                        viewModel.searchPosts(newValue.text)
                    }
                },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    if (searchQuery.text.isEmpty()) {
                        Text("Search posts...", color = Color.Gray)
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Filter Button
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add Post Button
            IconButton(onClick = { navController.navigate("createPost/$userID") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Post", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Filter Dialog
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onApplyFilter = { selectedDate, selectedCategoriesList ->
                    selectedDateRange = selectedDate
                    selectedCategories = selectedCategoriesList
                    showFilterDialog = false
                },
                currentDateFilter = selectedDateRange,
                currentCategoryFilter = selectedCategories
            )
        }





        // Loading Indicator
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        // Error Message
        else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $errorMessage", color = Color.Red, fontSize = 16.sp)
            }
        }
        // No Posts Available
        else if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No posts available", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        // Display Filtered Posts
        else {
            val displayPosts = if (searchQuery.text.isNotEmpty()) searchResults else posts

            if (displayPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No posts available", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(displayPosts) { post ->
                        val metrics = postMetrics[post.id] ?: PostMetrics(postId = post.id)
                        DiscussionPostItem(post, metrics, navController, viewModel)
                    }
                }
            }
        }
    }
}




// Filter Dialog
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (String, List<String>) -> Unit,
    currentDateFilter: String,
    currentCategoryFilter: List<String>
) {
    val dateOptions = listOf("All Time", "Past 24 Hours", "Past Week", "Past Month", "Past Year")
    var selectedDate by remember { mutableStateOf(currentDateFilter) }

    val categoryOptions = listOf("General Discussion", "Treatment & Medications", "Rehabilitation & Exercises", "Symptoms & Recovery", "Support & Personal Stories", "Diet & Lifestyle")
    var selectedCategories by remember { mutableStateOf(currentCategoryFilter) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Posts") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp) // add a bit of bottom space
            ) {
                Text("Date Range", fontWeight = FontWeight.Bold)
                dateOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDate = option }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDate == option,
                            onClick = { selectedDate = option }
                        )
                        Text(option, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Post Categories", fontWeight = FontWeight.Bold)
                categoryOptions.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategories = if (selectedCategories.contains(category)) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedCategories.contains(category),
                            onCheckedChange = {
                                selectedCategories = if (selectedCategories.contains(category)) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            }
                        )
                        Text(category, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApplyFilter(selectedDate, selectedCategories)
                onDismiss()
            }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


@Composable
fun DiscussionPostItem(
    post: Post,
    metrics: PostMetrics,
    navController: NavController,
    viewModel: ForumViewModel
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val username by viewModel.authorName.collectAsState()

    var isLiked by remember { mutableStateOf(metrics.likedBy.contains(userId)) }
    var likesCount by remember { mutableStateOf(metrics.likes) }
    var commentsCount by remember { mutableStateOf(metrics.comments) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(post.id) {
        viewModel.listenForCommentUpdates(post.id)
    }

    LaunchedEffect(metrics) {
        likesCount = metrics.likes
        commentsCount = metrics.comments
        isLiked = metrics.likedBy.contains(userId)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val imageBase64 = viewModel.userProfileImages[post.authorId]

                LaunchedEffect(post.authorId) {
                    viewModel.fetchUserProfileImage(post.authorId)
                }

                val imageBitmap = remember(imageBase64) {
                    imageBase64?.let {
                        try {
                            val bytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            bitmap?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Profile image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB0BEC5))
                    ) {
                        Text(
                            text = post.authorName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(formatTimestamp(post.timestamp), fontSize = 12.sp, color = Color.Gray)
                }

                if (post.authorId == userId) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete Post") },
                                onClick = {
                                    showMenu = false
                                    viewModel.deletePost(post.id,
                                        onSuccess = {
                                            val updatedPosts = viewModel.posts.value.filter { it.id != post.id }
                                            viewModel.updatePosts(updatedPosts)
                                        },
                                        onError = { error -> Log.e("ForumScreen", "Error deleting post: $error") }
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (post.content.length > 120) post.content.take(120) + "..." else post.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleLike(post.id, userId) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("$likesCount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { navController.navigate("postDetail/${post.id}/${userId}/$username") }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("$commentsCount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// Function to Format Timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}


