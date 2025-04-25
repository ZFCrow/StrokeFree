package com.example.strokefree.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.strokefree.ViewModels.ForumViewModel
import com.example.strokefree.classes.Comment
import com.example.strokefree.classes.Post
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    viewModel: ForumViewModel,
    postID: String,
    userID: String,
    userName: String,

) {
    /*LaunchedEffect(postID) {
        println("PostDetailScreen received postID: $postID") // Log in Logcat
    }*/
    var post by remember { mutableStateOf<Post?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var commentText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(postID) {
        coroutineScope.launch {
            viewModel.getPostDetails(postID,
                onSuccess = { fetchedPost -> post = fetchedPost },
                onError = { /* Handle error */ }
            )

            viewModel.getComments(postID,
                onSuccess = { fetchedComments -> comments = fetchedComments },
                onError = { /* Handle error */ }
            )

            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Post Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background) // ← replaces F9F9F9
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 64.dp) // Avoids overlap with input field
                    ) {
                        post?.let {
                            PostHeader(it, viewModel)
                            PostContent(it)
                            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                            CommentSection(comments, viewModel)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    CommentInputField(
                        text = commentText,
                        onTextChange = { commentText = it },
                        onSend = {
                            if (commentText.isNotEmpty()) {
                                val newComment = Comment(
                                    authorId = userID,
                                    authorName = userName,
                                    content = commentText,
                                    timestamp = System.currentTimeMillis()
                                )

                                viewModel.addComment(postID,
                                    newComment.content
                                ) { success, message ->
                                    if (success) {
                                        comments = comments + newComment
                                        commentText = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.White)
                    )
                }
            }
        }
    )
}


@Composable
fun PostHeader(post: Post, viewModel: ForumViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    .size(44.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB0BEC5))
            ) {
                Text(
                    text = post.authorName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = post.authorName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatTimestamp(post.timestamp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PostContent(post: Post) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = post.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = post.content,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        thickness = 1.dp
    )

    Text(
        text = "Comments",
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun CommentItem(comment: Comment, viewModel: ForumViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        val imageBase64 = viewModel.userProfileImages[comment.authorId]
        LaunchedEffect(comment.authorId) {
            viewModel.fetchUserProfileImage(comment.authorId)
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
                    .size(36.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB0BEC5))
            ) {
                Text(
                    text = comment.authorName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = comment.authorName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun CommentSection(comments: List<Comment>, viewModel: ForumViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        comments.forEach { comment ->
            CommentItem(comment, viewModel)
        }
    }
}

@Composable
fun CommentInputField(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                modifier = Modifier.weight(1f),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Write a comment...", color = Color.Gray)
                    }
                    innerTextField()
                }
            )
            IconButton(onClick = onSend) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color(0xFF007AFF)
                )
            }
        }
    }
}

