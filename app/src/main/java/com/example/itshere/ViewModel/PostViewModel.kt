package com.example.itshere.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.*
import com.example.itshere.Dao.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

data class PostState(
    val posts: List<PostData> = emptyList(),
    val drafts: List<PostData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadProgress: Float = 0f
)

class PostViewModel(context: Context) : ViewModel() {
    private val _state = MutableStateFlow(PostState())
    val state: StateFlow<PostState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val database = AppDatabase.getInstance(context)
    private val localImageDao = database.localImageDao()

    private val TAG = "PostViewModel"

    init {
        loadPosts()
        loadLocalDrafts()
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
                                    doc.toObject(PostData::class.java)?.copy(id = doc.id)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing post: ${e.message}")
                                    null
                                }
                            } ?: emptyList()

                            // 为每个帖子获取本地图片
                            val postsWithLocalImages = mutableListOf<PostData>()
                            for (post in posts) {
                                val localImages = localImageDao.getImagesByPostId(post.id)
                                    .firstOrNull() ?: emptyList()
                                val localImageUris = localImages.map { it.uri }

                                postsWithLocalImages.add(
                                    post.copy(localImageUris = localImageUris)
                                )
                            }

                            _state.value = _state.value.copy(
                                posts = postsWithLocalImages,
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

    fun loadLocalDrafts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val unuploadedImages = localImageDao.getAllUnuploadedImages().firstOrNull()
                if (unuploadedImages != null) {
                    val draftPosts = unuploadedImages.groupBy { it.postId }
                        .map { (postId, images) ->
                            PostData(
                                id = postId,
                                title = "Draft Post",
                                description = "Local draft with ${images.size} images",
                                localImageUris = images.map { it.uri },
                                isDraft = true,
                                isLocalOnly = true,
                                timestamp = images.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
                            )
                        }

                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(drafts = draftPosts)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading drafts: ${e.message}")
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
        isDraft: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, uploadProgress = 0f)

            try {
                val postId = UUID.randomUUID().toString()
                val currentUser = auth.currentUser

                if (currentUser == null && !isDraft) {
                    throw Exception("User not authenticated")
                }

                val savedImages = mutableListOf<com.example.itshere.Dao.LocalImage>()
                images.forEachIndexed { index, image ->
                    val localImage = com.example.itshere.Dao.LocalImage(
                        postId = postId,
                        uri = image.uri,
                        isUploaded = false
                    )
                    localImageDao.insert(localImage)
                    savedImages.add(localImage)

                    val progress = (index + 1).toFloat() / images.size.toFloat() * 0.3f
                    _state.value = _state.value.copy(uploadProgress = progress)
                }

                val localImageUris = savedImages.map { it.uri }
                val questionMaps = questions.map {
                    mapOf("question" to it.question, "answer" to it.answer)
                }

                val post = PostData(
                    id = postId,
                    userId = currentUser?.uid ?: "draft_user",
                    userName = currentUser?.displayName ?: "Anonymous",
                    title = title,
                    description = description,
                    postType = if (postType == PostType.FOUND) "FOUND" else "LOST",
                    phone = phone,
                    date = date,
                    category = category,
                    imageUrls = emptyList(),
                    localImageUris = localImageUris,
                    questions = questionMaps,
                    timestamp = System.currentTimeMillis(),
                    isDraft = isDraft,
                    isLocalOnly = isDraft,
                    needsUpload = !isDraft
                )

                if (isDraft) {
                    val currentDrafts = _state.value.drafts.toMutableList()
                    currentDrafts.add(0, post)
                    _state.value = _state.value.copy(
                        drafts = currentDrafts,
                        isLoading = false,
                        uploadProgress = 1f
                    )

                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    uploadPostToFirebase(post, savedImages, onSuccess, onError)
                }

            } catch (e: Exception) {
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

    private suspend fun uploadPostToFirebase(
        post: PostData,
        localImages: List<com.example.itshere.Dao.LocalImage>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val uploadedImageUrls = mutableListOf<String>()
            val totalImages = localImages.size

            localImages.forEachIndexed { index, localImage ->
                try {
                    val downloadUrl = uploadImageToFirebase(localImage.uri)
                    if (downloadUrl != null) {
                        uploadedImageUrls.add(downloadUrl)
                        localImageDao.markAsUploaded(localImage.id)

                        val progress = 0.3f + (index + 1).toFloat() / totalImages.toFloat() * 0.6f
                        _state.value = _state.value.copy(uploadProgress = progress)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to upload image ${localImage.id}: ${e.message}")
                }
            }

            val finalPost = post.copy(
                imageUrls = uploadedImageUrls,
                localImageUris = emptyList(),
                isLocalOnly = false,
                needsUpload = false
            )

            firestore.collection("posts")
                .document(post.id)
                .set(finalPost)
                .await()

            Log.d(TAG, "✓ Post saved to Firestore: ${post.id}")

            val currentPosts = _state.value.posts.toMutableList()
            currentPosts.add(0, finalPost)

            _state.value = _state.value.copy(
                posts = currentPosts,
                isLoading = false,
                uploadProgress = 1f
            )

            withContext(Dispatchers.Main) {
                onSuccess()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading post: ${e.message}", e)
            _state.value = _state.value.copy(isLoading = false, uploadProgress = 0f)
            withContext(Dispatchers.Main) {
                onError("Failed to upload post: ${e.message}")
            }
        }
    }

    private suspend fun uploadImageToFirebase(uriString: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val fileName = "posts/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)

                val uploadTask = storageRef.putFile(uri)
                val taskSnapshot = uploadTask.await()
                val downloadUrl = storageRef.downloadUrl.await()

                downloadUrl.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed: ${e.message}")
                null
            }
        }
    }

    fun saveDraft(
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
        createPost(
            title = title,
            description = description,
            postType = postType,
            phone = phone,
            date = date,
            category = category,
            images = images,
            questions = questions,
            isDraft = true,
            onSuccess = onSuccess,
            onError = onError
        )
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
                } else {
                    favoriteRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite: ${e.message}")
            }
        }
    }

    suspend fun getPostImages(postId: String): List<String> {
        return withContext(Dispatchers.IO) {
            val post = _state.value.posts.find { it.id == postId }
            if (post != null) {
                if (post.imageUrls.isNotEmpty()) {
                    post.imageUrls
                } else {
                    post.localImageUris
                }
            } else {
                val localImages = localImageDao.getImagesByPostId(postId).firstOrNull()
                localImages?.map { it.uri } ?: emptyList()
            }
        }
    }

    fun deleteDraft(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                localImageDao.deleteByPostId(postId)

                withContext(Dispatchers.Main) {
                    val updatedDrafts = _state.value.drafts.filter { it.id != postId }
                    _state.value = _state.value.copy(drafts = updatedDrafts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting draft: ${e.message}")
            }
        }
    }
}