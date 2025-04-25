package com.example.strokefree.classes

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    val id: String = "",  // Auto-generated ID from Firebase
    val authorId: String = "",  // User ID of the author
    val authorName: String = "",
    val title: String = "",
    val content: String = "",
    val categories: List<String> = emptyList(),  // Tags for post classification
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val visibility: PostVisibility = PostVisibility.PUBLIC,
    val status: PostStatus = PostStatus.ACTIVE,
    val keywords: List<String> = emptyList()
)

data class PostMetrics(
    val postId: String = "",  // ID of the associated post
    val likes: Int = 0,
    val comments: Int = 0,
    val favourites: Int = 0,
    val views: Int = 0,
    val trendingScore: Int = 0, // Used for classifying "Trending" posts
    val likedBy: List<String> = emptyList()
)

data class Comment(
    val commentId: String = "",  // Auto-generated Firebase ID
    val postId: String = "",  // Reference to parent post
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class PostCategory {
    DISCUSSION,
    TRENDING,
    GENERAL,
    ANNOUNCEMENT,
    HELP
}

enum class PostVisibility {
    PUBLIC,  // Visible to all users
    PRIVATE  // Only visible to the author
}

enum class PostStatus {
    ACTIVE,
    DELETED
}