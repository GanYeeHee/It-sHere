package com.example.itshere.ViewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.*
import com.example.itshere.Dao.AppDatabase
import com.example.itshere.Dao.LocalImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val database = AppDatabase.getInstance(context)
    private val localImageDao = database.localImageDao()

    private val TAG = "PostViewModel"

    init {
        Log.d(TAG, "🎯 PostViewModel initialized - LOCAL FILE PATH VERSION")
        loadPosts()
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

                            Log.d(TAG, "📦 Loaded ${posts.size} posts")

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
            Log.d(TAG, "🚀 Starting createPost - USING LOCAL FILE PATHS")
            _state.value = _state.value.copy(isLoading = true, uploadProgress = 0f)

            try {
                val postId = UUID.randomUUID().toString()
                val currentUser = auth.currentUser

                if (currentUser == null) {
                    throw Exception("User not authenticated")
                }

                Log.d(TAG, "📸 Processing ${images.size} images")

                // ✅ 複製圖片並獲取絕對文件路徑
                val filePaths = mutableListOf<String>()
                images.forEachIndexed { index, image ->
                    try {
                        val filePath = copyImageAndGetFilePath(image.uri, postId, index)
                        if (filePath != null) {
                            filePaths.add(filePath)
                            Log.d(TAG, "  ✓ Copied image $index: $filePath")

                            val progress = (index + 1).toFloat() / images.size.toFloat()
                            _state.value = _state.value.copy(uploadProgress = progress)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to copy image: ${e.message}", e)
                    }
                }

                if (filePaths.isEmpty()) {
                    throw Exception("Failed to save images")
                }

                Log.d(TAG, "💾 Saving images to Room database")
                val localImages = filePaths.map { path ->
                    LocalImage(
                        postId = postId,
                        uri = path,  // ✅ 存儲絕對文件路徑
                        timestamp = System.currentTimeMillis(),
                        isUploaded = false
                    )
                }
                localImageDao.insertAll(localImages)
                Log.d(TAG, "✓ Saved ${localImages.size} images to Room")

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
                    imageUrls = filePaths,  // ✅ 使用絕對文件路徑
                    questions = questionMaps,
                    timestamp = System.currentTimeMillis()
                )

                Log.d(TAG, "☁️ Saving post to Firestore")
                Log.d(TAG, "  Post ID: $postId")
                Log.d(TAG, "  Image Paths: ${post.imageUrls}")

                firestore.collection("posts")
                    .document(postId)
                    .set(post)
                    .await()

                Log.d(TAG, "✅ Post saved successfully!")

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
                Log.e(TAG, "❌ Error creating post: ${e.message}", e)
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

    /**
     * 複製圖片到 app 私有存儲,返回絕對文件路徑
     */
    private suspend fun copyImageAndGetFilePath(
        uriString: String,
        postId: String,
        index: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            val sourceUri = Uri.parse(uriString)

            // 創建存儲目錄
            val imagesDir = File(context.filesDir, "post_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            // 創建目標文件
            val fileName = "${postId}_$index.jpg"
            val destFile = File(imagesDir, fileName)

            // 複製文件
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // ✅ 返回絕對文件路徑
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying image: ${e.message}", e)
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
                } else {
                    favoriteRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite: ${e.message}")
            }
        }
    }
}