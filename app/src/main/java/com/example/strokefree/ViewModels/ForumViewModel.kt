package com.example.strokefree.ViewModels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strokefree.classes.Comment
import com.example.strokefree.classes.Post
import com.example.strokefree.classes.PostMetrics
import com.example.strokefree.classes.PostStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar


class ForumViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val postsCollection = db.collection("posts")
    private val metricsCollection = db.collection("postMetrics")

    private val _authorName = MutableStateFlow("")
    val authorName: StateFlow<String> = _authorName

    private val _searchResults = MutableStateFlow<List<Post>>(emptyList())
    val searchResults: StateFlow<List<Post>> = _searchResults

    private val _postMetrics = MutableStateFlow<Map<String, PostMetrics>>(emptyMap())
    val postMetrics: StateFlow<Map<String, PostMetrics>> = _postMetrics

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    val userProfileImages = mutableStateMapOf<String, String?>()

    var userID by mutableStateOf("")

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        val currentUser = auth.currentUser

        if (currentUser != null ) {
            userID = currentUser.uid

        }
    }

    init {
        // Attach the auth state listener
        firebaseAuth.addAuthStateListener(authListener)
        getCurrentUserName() // for the post

    }

    fun updatePosts(newPosts: List<Post>) {
        Log.d("ForumViewModel", "Updating posts: ${newPosts.size} posts")
        _posts.value = newPosts
    }


    /** Fetch Current User's Name */
    private fun getCurrentUserName() {
        val user = auth.currentUser
        if (user != null) {
            Log.d("ForumViewModel", "FirebaseAuth Display Name: ${user.displayName}")

            // 🔹 First try to get from FirebaseAuth
            if (!user.displayName.isNullOrBlank()) {
                _authorName.value = user.displayName!!
                Log.d("ForumViewModel", "Using Auth Display Name: ${_authorName.value}")
                return
            }

            // 🔹 If displayName is missing, check Firestore
            val uid = user.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firestoreName = document.getString("name") ?: "Anonymous"
                        _authorName.value = firestoreName
                        Log.d("ForumViewModel", "Using Firestore Name: $firestoreName")
                    } else {
                        Log.w("ForumViewModel", "No Firestore document found for user.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ForumViewModel", "Error fetching user name from Firestore", e)
                }
        } else {
            Log.e("ForumViewModel", "No authenticated user found!")
        }
    }


    /** Fetch All Active Posts */
    fun getAllActivePosts(onSuccess: (List<Post>) -> Unit, onError: (String) -> Unit) {
        postsCollection
            .whereEqualTo("status", PostStatus.ACTIVE.name)
            .get()
            .addOnSuccessListener { result ->
                val posts = result.toObjects(Post::class.java)
                    .sortedByDescending { it.timestamp }

                Log.d("ForumViewModel", "Fetched ${posts.size} posts")
                posts.forEach { Log.d("ForumViewModel", "Post: ${it.id}, Title: ${it.title}") }

                onSuccess(posts)
            }
            .addOnFailureListener { exception ->
                Log.e("ForumViewModel", "Error fetching posts: ${exception.localizedMessage}")
                onError("Failed to load posts: ${exception.localizedMessage}")
            }
    }




    /** Fetch All Post Metrics */
    fun getAllPostMetrics(
        postIds: List<String>,
        onSuccess: (List<PostMetrics>) -> Unit, // Ensure this is a List<PostMetrics>
        onError: (String) -> Unit
    ) {
        if (postIds.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        db.collection("postMetrics")
            .whereIn("postId", postIds)
            .get()
            .addOnSuccessListener { result ->
                val metricsList = result.toObjects(PostMetrics::class.java)
                onSuccess(metricsList)
            }
            .addOnFailureListener { exception ->
                onError("Error fetching post metrics: ${exception.localizedMessage}")
            }
    }



    private fun generateKeywords(text: String): List<String> {
        return text.lowercase()
            .split("\\s+".toRegex())  // Split by spaces
            .map { it.replace("[^a-zA-Z0-9]".toRegex(), "") }  // Remove special characters
            .filter { it.isNotEmpty() }  // Remove empty strings
            .distinct()  // Remove duplicates
    }

    /** Create a New Post */
    fun createPost(post: Post, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val newPostRef = postsCollection.document()

                // Generate keywords from title & content
                val keywords = generateKeywords(post.title) + generateKeywords(post.content)
                val postWithKeywords = post.copy(id = newPostRef.id, keywords = keywords)

                // Save post with keywords
                newPostRef.set(postWithKeywords).await()

                // Initialize post metrics
                val initialMetrics = PostMetrics(postId = newPostRef.id)
                metricsCollection.document(newPostRef.id).set(initialMetrics).await()

                onResult(true)
            } catch (e: Exception) {
                Log.e("ForumViewModel", "Error creating post", e)
                onResult(false)
            }
        }
    }

    fun searchPosts(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        postsCollection
            .whereArrayContains("keywords", query.lowercase().trim())
            .get()
            .addOnSuccessListener { result ->
                _searchResults.value = result.toObjects(Post::class.java)
            }
            .addOnFailureListener { exception ->
                Log.e("ForumViewModel", "Search failed: ${exception.localizedMessage}")
            }
    }

    fun updatePostMetrics(metricsMap: Map<String, PostMetrics>) {
        _postMetrics.value = metricsMap
    }


    fun getFilteredPostsByDateAndCategory(
        dateRange: String,
        selectedCategories: List<String>,
        onSuccess: (List<Post>) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val startTime = when (dateRange) {
            "Past 24 Hours" -> currentTime - (24 * 60 * 60 * 1000)
            "Past Week" -> currentTime - (7 * 24 * 60 * 60 * 1000)
            "Past Month" -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            "Past Year" -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            else -> null
        }

        postsCollection.whereEqualTo("status", PostStatus.ACTIVE.name)
            .get()
            .addOnSuccessListener { result ->
                var filteredPosts = result.toObjects(Post::class.java)

                if (dateRange != "All Time" && startTime != null) {
                    filteredPosts = filteredPosts.filter { it.timestamp in startTime..currentTime }
                }

                if (selectedCategories.isNotEmpty()) {
                    filteredPosts = filteredPosts.filter { post ->
                        post.categories.any { it in selectedCategories }
                    }
                }

                filteredPosts = filteredPosts.sortedByDescending { it.timestamp }
                onSuccess(filteredPosts)
            }
            .addOnFailureListener { exception ->
                onError("Error fetching posts: ${exception.localizedMessage}")
            }
    }



    /** Fetch post details by postId */
    suspend fun getPostDetails(postId: String, onSuccess: (Post) -> Unit, onError: (String) -> Unit) {
        try {
            val doc = db.collection("posts").document(postId).get().await()
            doc?.let {
                val post = it.toObject(Post::class.java)?.copy(id = it.id)
                if (post != null) {
                    onSuccess(post)
                } else {
                    onError("Post not found")
                }
            }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Error fetching post")
        }
    }

    /** Fetch comments for a given postId */
    suspend fun getComments(postId: String, onSuccess: (List<Comment>) -> Unit, onError: (String) -> Unit) {
        try {
            val querySnapshot = db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get().await()

            val comments = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)?.copy(commentId = doc.id)
            }

            onSuccess(comments)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Error fetching comments")
        }
    }

    /** Add a comment */
    fun addComment(postId: String, commentContent: String, onComplete: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onComplete(false, "User not authenticated")
            return
        }

        val userId = user.uid
        val authDisplayName = user.displayName

        val userDocRef = db.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            val firestoreName = document.getString("name") ?: "Anonymous"
            val finalUserName = authDisplayName ?: firestoreName

            val postRef = db.collection("posts").document(postId)
            val commentsRef = postRef.collection("comments").document()
            val postMetricsRef = db.collection("postMetrics").document(postId)

            val newComment = Comment(
                commentId = commentsRef.id,
                postId = postId,
                authorId = userId,
                authorName = finalUserName,
                content = commentContent,
                timestamp = System.currentTimeMillis()
            )

            db.runTransaction { transaction ->
                val snapshot = transaction.get(postMetricsRef)

                if (!snapshot.exists()) {
                    throw Exception("Post metrics not found")
                }

                var commentsCount = snapshot.getLong("comments") ?: 0
                commentsCount += 1

                transaction.set(commentsRef, newComment)
                transaction.update(postMetricsRef, "comments", commentsCount)
            }.addOnSuccessListener {
                onComplete(true, "Comment added successfully")
            }.addOnFailureListener { exception ->
                Log.e("ForumViewModel", "Error adding comment: ${exception.localizedMessage}")
                onComplete(false, exception.localizedMessage ?: "Error adding comment")
            }

        }.addOnFailureListener { exception ->
            onComplete(false, "Failed to fetch user data: ${exception.localizedMessage}")
        }
    }


    fun listenForCommentUpdates(postId: String) {
        val postMetricsRef = db.collection("postMetrics").document(postId)

        postMetricsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                Log.e("ForumViewModel", "Error listening for comments: ${error?.localizedMessage}")
                return@addSnapshotListener
            }

            val updatedComments = (snapshot.getLong("comments") ?: 0).toInt()


            val updatedMetrics = _postMetrics.value.orEmpty().toMutableMap()
            updatedMetrics[postId] = updatedMetrics[postId]?.copy(comments = updatedComments)
                ?: PostMetrics(postId = postId, comments = updatedComments)

            _postMetrics.value = updatedMetrics
        }
    }




    fun toggleLike(postId: String, userId: String) {
        val postMetricsRef = db.collection("postMetrics").document(postId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(postMetricsRef)

            if (!snapshot.exists()) {
                throw Exception("Post metrics not found")
            }

            val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
            val mutableLikedBy = likedBy.toMutableList()
            var likesCount = snapshot.getLong("likes") ?: 0

            if (mutableLikedBy.contains(userId)) {
                mutableLikedBy.remove(userId)
                likesCount = maxOf(0, likesCount - 1) // Prevent negative likes
            } else {
                mutableLikedBy.add(userId)
                likesCount += 1
            }

            transaction.update(postMetricsRef, mapOf(
                "likedBy" to mutableLikedBy,
                "likes" to likesCount
            ))

            return@runTransaction likesCount
        }.addOnSuccessListener { updatedLikes ->
            val updatedMetrics = _postMetrics.value.toMutableMap()
            val currentMetrics = updatedMetrics[postId] ?: PostMetrics(postId = postId, likes = 0, likedBy = emptyList())

            updatedMetrics[postId] = currentMetrics.copy(
                likes = updatedLikes.toInt(),
                likedBy = if (currentMetrics.likedBy.contains(userId)) {
                    currentMetrics.likedBy - userId
                } else {
                    currentMetrics.likedBy + userId
                }
            )

            _postMetrics.value = updatedMetrics
        }.addOnFailureListener { exception ->
            Log.e("ForumViewModel", "Error updating like status: ${exception.localizedMessage}")
        }
    }

    fun fetchPosts() {
        getAllActivePosts(
            onSuccess = { fetchedPosts -> _posts.value = fetchedPosts },
            onError = { error -> Log.e("ForumViewModel", "Error fetching posts: $error") }
        )
    }

    fun deletePost(postId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val postRef = db.collection("posts").document(postId)
        val commentsRef = postRef.collection("comments")
        val postMetricsRef = db.collection("postMetrics").document(postId)

        // Delete all comments first
        commentsRef.get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                for (document in snapshot.documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        postRef.delete()
                            .addOnSuccessListener {
                                postMetricsRef.delete()
                                    .addOnSuccessListener {
                                        _posts.value = _posts.value.filter { it.id != postId }
                                        onSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("ForumViewModel", "Error deleting post metrics: ${e.localizedMessage}")
                                        onError("Error deleting post metrics")
                                    }
                            }
                            .addOnFailureListener { e -> onError("Error deleting post: ${e.localizedMessage}") }
                    }
                    .addOnFailureListener { e -> onError("Error deleting comments: ${e.localizedMessage}") }
            }
            .addOnFailureListener { e -> onError("Error fetching comments: ${e.localizedMessage}") }
    }

    fun fetchUserProfileImage(uid: String) {
        if (userProfileImages.contains(uid)) return

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val image = doc.getString("imageURL")
                userProfileImages[uid] = image
            }
            .addOnFailureListener {
                userProfileImages[uid] = null
            }
    }





}
