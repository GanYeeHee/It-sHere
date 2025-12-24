package com.example.itshere.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.*
import com.example.itshere.data.*
import com.example.itshere.data.AppDatabase
import com.example.itshere.data.entity.LocalImage
import com.example.itshere.data.entity.Notification
import com.example.itshere.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

data class PostState(
    val posts: List<PostData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadProgress: Float = 0f
)

class PostViewModel(private val context: Context) : ViewModel() {
    private val _state = MutableStateFlow(PostState())
    val state: StateFlow<PostState> = _state
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getInstance(context)
    private val userRepository = UserRepository(database)
    private val localImageDao = database.localImageDao()
    private val tag = "PostViewModel"
    private val _requests = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val requests: StateFlow<List<ClaimRequest>> = _requests.asStateFlow()
    private val _approvedCount = MutableStateFlow(0)
    val approvedCount: StateFlow<Int> = _approvedCount.asStateFlow()
    private val _rejectedCount = MutableStateFlow(0)
    val rejectedCount: StateFlow<Int> = _rejectedCount.asStateFlow()
    private val _approvedClaims = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val approvedClaims: StateFlow<List<ClaimRequest>> = _approvedClaims.asStateFlow()

    private val _rejectedClaims = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val rejectedClaims: StateFlow<List<ClaimRequest>> = _rejectedClaims.asStateFlow()

    //NotificationManager
    private var notificationManager: NotificationManager

    init {
        Log.d(tag, "PostViewModel initialized")

        // 初始化 NotificationManager
        notificationManager = NotificationManager(context)

        // 检查通知权限
        checkNotificationPermission()

        loadPosts()
        loadFavorites()
        loadClaimRequests()
        observeApprovedClaims() // New observer
        observeRejectedClaims() // New observer
    }

    private fun checkNotificationPermission() {
        viewModelScope.launch {
            val enabled = withContext(Dispatchers.Main) {
                notificationManager.areNotificationsEnabled()
            }
            Log.d(tag, "Notifications enabled: $enabled")
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        viewModelScope.launch {
                            if (error != null) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = error.message
                                )
                                return@launch
                            }

                            val posts = snapshot?.documents?.mapNotNull { doc ->
                                try {
                                    val post = doc.toObject(PostData::class.java)?.copy(id = doc.id)

                                    post?.copy(isFavorite = _favorites.value.contains(post.id))
                                } catch (e: Exception) {
                                    Log.e(tag, "Error parsing post: ${e.message}")
                                    null
                                }
                            } ?: emptyList()

                            Log.d(tag, "Loaded ${posts.size} posts")

                            _state.value = _state.value.copy(
                                posts = posts,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val currentUser = auth.currentUser ?: return@launch
            try {
                firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("favorites")
                    .addSnapshotListener { snapshot, _ ->
                        snapshot?.let {
                            val favoriteIds = it.documents.map { doc -> doc.id }.toSet()
                            _favorites.value = favoriteIds
                            Log.d(tag, "Loaded ${favoriteIds.size} favorites")

                            // 更新 posts 中的收藏狀態
                            val updatedPosts = _state.value.posts.map { post ->
                                post.copy(isFavorite = favoriteIds.contains(post.id))
                            }
                            _state.value = _state.value.copy(posts = updatedPosts)
                        }
                    }
            } catch (e: Exception) {
                Log.e(tag, "Error loading favorites: ${e.message}")
            }
        }
    }

    fun createPost(
        title: String,
        description: String,
        postType: PostType,
        phone: String,
        date: String,
        category: String,
        images: List<ImageItem>,
        questions: List<QuestionAnswer>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d(tag, "Starting createPost - USING LOCAL FILE PATHS")
            _state.value = _state.value.copy(isLoading = true, uploadProgress = 0f)

            try {
                val postId = UUID.randomUUID().toString()
                val currentUser = auth.currentUser

                if (currentUser == null) {
                    throw Exception("User not authenticated")
                }

                Log.d(tag, "Processing ${images.size} images")

                val filePaths = mutableListOf<String>()
                images.forEachIndexed { index, image ->
                    try {
                        val filePath = copyImageAndGetFilePath(image.uri, postId, index)
                        if (filePath != null) {
                            filePaths.add(filePath)
                            Log.d(tag, "  ✓ Copied image $index: $filePath")

                            val progress = (index + 1).toFloat() / images.size.toFloat()
                            _state.value = _state.value.copy(uploadProgress = progress)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to copy image: ${e.message}", e)
                    }
                }

                if (filePaths.isEmpty()) {
                    throw Exception("Failed to save images")
                }

                Log.d(tag, "Saving images to Room database")
                val localImages = filePaths.map { path ->
                    LocalImage(
                        postId = postId,
                        uri = path,
                        timestamp = System.currentTimeMillis(),
                        isUploaded = false
                    )
                }
                localImageDao.insertAll(localImages)
                Log.d(tag, "✓ Saved ${localImages.size} images to Room")

                val questionMaps = questions.map {
                    mapOf("question" to it.question, "answer" to it.answer)
                }

                val post = PostData(
                    id = postId,
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "Anonymous",
                    title = title,
                    description = description,
                    postType = if (postType == PostType.FOUND) "FOUND" else "LOST",
                    phone = phone,
                    date = date,
                    category = category,
                    imageUrls = filePaths,
                    questions = questionMaps,
                    timestamp = System.currentTimeMillis(),
                    isFavorite = false
                )

                Log.d(tag, "Saving post to Firestore")
                Log.d(tag, "  Post ID: $postId")
                Log.d(tag, "  Image Paths: ${post.imageUrls}")

                firestore.collection("posts")
                    .document(postId)
                    .set(post)
                    .await()

                Log.d(tag, "Post saved successfully!")

                try {
                    val notification = Notification(
                        postId = postId,
                        postTitle = title,
                        postType = if (postType == PostType.FOUND) "FOUND" else "LOST",
                        postCategory = category,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )

                    withContext(Dispatchers.IO) {
                        database.notificationDao().insert(notification)
                    }

                    Log.d(tag, "✓ Notification saved to database")

                    notificationManager.showNewPostNotification(
                        postId = postId,
                        postTitle = title,
                        postType = if (postType == PostType.FOUND) "Found" else "Lost",
                        category = category
                    )

                    Log.d(tag, "✓ System notification triggered")

                } catch (e: Exception) {
                    Log.e(tag, "Failed to create/display notification: ${e.message}", e)
                }

                val currentPosts = _state.value.posts.toMutableList()
                currentPosts.add(0, post)

                _state.value = _state.value.copy(
                    posts = currentPosts,
                    isLoading = false,
                    uploadProgress = 1f
                )

                withContext(Dispatchers.Main) {
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(tag, "Error creating post: ${e.message}", e)
                e.printStackTrace()
                _state.value = _state.value.copy(
                    isLoading = false,
                    uploadProgress = 0f
                )
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to create post")
                }
            }
        }
    }

    private suspend fun copyImageAndGetFilePath(
        uriString: String,
        postId: String,
        index: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            val sourceUri = Uri.parse(uriString)

            val imagesDir = File(context.filesDir, "post_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val fileName = "${postId}_$index.jpg"
            val destFile = File(imagesDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(tag, "Error copying image: ${e.message}", e)
            null
        }
    }

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: return@launch
                val favoriteRef = firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("favorites")
                    .document(postId)

                val doc = favoriteRef.get().await()
                if (doc.exists()) {
                    favoriteRef.delete().await()

                    val newFavorites = _favorites.value - postId
                    _favorites.value = newFavorites
                } else {
                    favoriteRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()

                    val newFavorites = _favorites.value + postId
                    _favorites.value = newFavorites
                }

                val updatedPosts = _state.value.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(isFavorite = !post.isFavorite)
                    } else {
                        post
                    }
                }
                _state.value = _state.value.copy(posts = updatedPosts)

            } catch (e: Exception) {
                Log.e(tag, "Error toggling favorite: ${e.message}")
            }
        }
    }

    fun submitClaimRequest(postId: String, postTitle: String, answers: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val firebaseUid = auth.currentUser?.uid ?: return@launch
                val roomUser = userRepository.getUserByFirebaseUid(firebaseUid)

                // Create a document reference first to get its unique ID
                val docRef = firestore.collection("claims").document()

                val newRequest = ClaimRequest(
                    id = docRef.id, // STORE THE GENERATED ID HERE
                    postId = postId,
                    postTitle = postTitle,
                    requesterId = roomUser?.userId ?: "U_Unknown",
                    requesterName = roomUser?.name ?: "",
                    answers = answers,
                    timestamp = System.currentTimeMillis()
                )

                docRef.set(newRequest).await() // Use .set instead of .add
                Log.d("PostViewModel", "Claim submitted with ID: ${docRef.id}")
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error: ${e.message}")
            }
        }
    }

    fun loadClaimRequests() {
        firestore.collection("claims")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        // THE .copy(id = doc.id) IS CRITICAL
                        doc.toObject(ClaimRequest::class.java)?.copy(id = doc.id)
                    }
                    _requests.value = list
                }
            }
    }

    fun getPostById(postId: String): PostData? {
        return _state.value.posts.find { it.id == postId }
    }

    fun processClaim(request: ClaimRequest, status: String) {
        // Add a check to prevent empty ID errors
        if (request.id.isEmpty()) {
            Log.e("PostViewModel", "Error: Claim Request ID is empty!")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Determine the destination collection based on button pressed
                val destination = if (status == "approve") "approved_claims" else "rejected_claims"
                val batch = firestore.batch()

                val newRef = firestore.collection(destination).document(request.id)
                batch.set(newRef, request)

                val oldRef = firestore.collection("claims").document(request.id)
                batch.delete(oldRef)

                batch.commit().await()
                Log.d("PostViewModel", "Successfully moved ${request.id} to $destination")
            } catch (e: Exception) {
                Log.e("PostViewModel", "Firestore Batch Failed: ${e.message}")
            }
        }
    }
    private fun observeApprovedClaims() {
        firestore.collection("approved_claims")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(ClaimRequest::class.java)?.copy(id = it.id) } ?: emptyList()
                _approvedClaims.value = list
                _approvedCount.value = list.size
            }
    }

    private fun observeRejectedClaims() {
        firestore.collection("rejected_claims")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(ClaimRequest::class.java)?.copy(id = it.id) } ?: emptyList()
                _rejectedClaims.value = list
                _rejectedCount.value = list.size
            }
    }

    fun testNotification() {
        viewModelScope.launch {
            notificationManager.showNewPostNotification(
                postId = "test_${System.currentTimeMillis()}",
                postTitle = "Test Notification",
                postType = "Found",
                category = "Test"
            )
        }
    }
}