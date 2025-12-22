package com.example.itshere.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.*
import com.example.itshere.Data.AppDatabase
import com.example.itshere.Data.Entity.LocalImage
import com.example.itshere.Repository.UserRepository
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


    init {
        Log.d(tag, "PostViewModel initialized - LOCAL FILE PATH VERSION")
        loadPosts()
        loadFavorites()
        loadClaimRequests()
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

                val newRequest = ClaimRequest(
                    postId = postId,
                    postTitle = postTitle,
                    requesterId = roomUser?.userId ?: "U_Unknown",
                    requesterName = roomUser?.name ?: "",
                    answers = answers,
                    timestamp = System.currentTimeMillis()
                )

                firestore.collection("claims").add(newRequest).await()
                Log.d("PostViewModel", "Claim submitted successfully")
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error: ${e.message}")
            }
        }
    }

    fun loadClaimRequests() {
        firestore.collection("claims")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PostViewModel", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // If the collection is cleared, 'documents' will be empty
                    // Setting this to emptyList() is what clears your screen!
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ClaimRequest::class.java)?.copy(id = doc.id)
                    }
                    _requests.value = list
                    Log.d("PostViewModel", "Updated requests list. Size: ${list.size}")
                }
            }
    }
    fun clearAllRequests() {
        // 1. We stay on the Main Thread initially to safely clear the UI
        viewModelScope.launch {
            try {
                // 2. Clear the UI list FIRST
                // This stops the LazyColumn from drawing, so it won't crash when data vanishes
                _requests.value = emptyList()

                // 3. Switch to the IO thread ONLY for the network call
                withContext(Dispatchers.IO) {
                    val collectionRef = firestore.collection("claims")
                    val snapshot = collectionRef.get().await()

                    if (snapshot.isEmpty) return@withContext

                    val batch = firestore.batch()
                    for (document in snapshot.documents) {
                        batch.delete(document.reference)
                    }

                    // 4. Commit the deletion to the cloud
                    batch.commit().await()
                    Log.d("PostViewModel", "Firestore claims cleared successfully")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Clear failed: ${e.message}")
                // If it fails, reload the data so the UI reflects reality
                loadClaimRequests()
            }
        }
    }

    fun getPostById(postId: String): PostData? {
        return _state.value.posts.find { it.id == postId }
    }
}



