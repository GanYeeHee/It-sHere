package com.example.itshere.ViewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.ImageItem
import com.example.itshere.Data.PostType
import com.example.itshere.Data.QuestionAnswer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class PostData(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val postType: String = "FOUND", // "LOST" or "FOUND"
    val phone: String = "",
    val date: String = "",
    val category: String = "",
    val imageUrls: List<String> = emptyList(),
    val questions: List<Map<String, String>> = emptyList(), // [{"question": "...", "answer": "..."}]
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

data class PostState(
    val posts: List<PostData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PostViewModel : ViewModel() {
    private val _state = MutableStateFlow(PostState())
    val state: StateFlow<PostState> = _state

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = error.message
                            )
                            return@addSnapshotListener
                        }

                        val posts = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(PostData::class.java)?.copy(id = doc.id)
                        } ?: emptyList()

                        _state.value = _state.value.copy(
                            posts = posts,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private val TAG = "PostViewModel"

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
        Log.d(TAG, "=== CREATE POST START ===")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Description: $description")
        Log.d(TAG, "Post Type: $postType")
        Log.d(TAG, "Images count: ${images.size}")

        images.forEachIndexed { index, image ->
            Log.d(TAG, "Image $index URI: ${image.uri}")
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val imageUrls = mutableListOf<String>()

                Log.d(TAG, "Starting image upload process...")
                for ((index, image) in images.withIndex()) {
                    Log.d(TAG, "Uploading image $index: ${image.uri}")

                    delay(100)

                    val downloadUrl = uploadImage(image.uri)
                    if (downloadUrl != null) {
                        imageUrls.add(downloadUrl)
                        Log.d(TAG, "✓ Image $index uploaded successfully: $downloadUrl")
                    } else {
                        Log.e(TAG, "✗ Image $index upload failed!")
                    }
                }

                Log.d(TAG, "Total successful uploads: ${imageUrls.size}")
                Log.d(TAG, "Image URLs to save: $imageUrls")

                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Log.e(TAG, "User not authenticated!")
                    throw Exception("User not authenticated")
                }

                val questionMaps = questions.map {
                    mapOf("question" to it.question, "answer" to it.answer)
                }

                val post = PostData(
                    id = UUID.randomUUID().toString(),
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: "Anonymous",
                    title = title,
                    description = description,
                    postType = if (postType == PostType.FOUND) "FOUND" else "LOST",
                    phone = phone,
                    date = date,
                    category = category,
                    imageUrls = imageUrls,
                    questions = questionMaps,
                    timestamp = System.currentTimeMillis()
                )

                Log.d(TAG, "Post object created:")
                Log.d(TAG, "- ID: ${post.id}")
                Log.d(TAG, "- Title: ${post.title}")
                Log.d(TAG, "- Image URLs count: ${post.imageUrls.size}")

                // 3. 保存到 Firestore
                Log.d(TAG, "Saving to Firestore...")
                firestore.collection("posts")
                    .document(post.id)
                    .set(post)
                    .addOnSuccessListener {
                        Log.d(TAG, "✓ Post saved to Firestore successfully!")
                        Log.d(TAG, "Document ID: ${post.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "✗ Failed to save post to Firestore: ${e.message}")
                    }
                    .await()

                Log.d(TAG, "✓ Post saved to Firestore: ${post.id}")

                val currentPosts = _state.value.posts.toMutableList()
                currentPosts.add(0, post)
                _state.value = _state.value.copy(
                    posts = currentPosts,
                    isLoading = false
                )

                Log.d(TAG, "✓ Local state updated")

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Calling onSuccess callback")
                    onSuccess()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error creating post: ${e.message}", e)
                _state.value = _state.value.copy(isLoading = false)
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to create post")
                }
            }
            Log.d(TAG, "=== CREATE POST END ===")
        }
    }


    private suspend fun uploadImage(uriString: String): String? {
        return try {
            Log.d(TAG, "=== UPLOAD IMAGE START ===")
            Log.d(TAG, "Input URI: $uriString")

            val uri = Uri.parse(uriString)
            Log.d(TAG, "Parsed URI: $uri")

            val fileName = "posts/${UUID.randomUUID()}.jpg"
            Log.d(TAG, "File name: $fileName")

            val storageRef = storage.reference.child(fileName)
            Log.d(TAG, "Storage reference: $storageRef")

            Log.d(TAG, "Starting upload...")
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d(TAG, "Upload progress: $progress%")
            }

            val taskSnapshot = uploadTask.await()
            Log.d(TAG, "✓ Upload completed successfully")
            Log.d(TAG, "Task snapshot: $taskSnapshot")

            Log.d(TAG, "Getting download URL...")
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d(TAG, "✓ Download URL obtained: $downloadUrl")

            downloadUrl.toString()

        } catch (e: Exception) {
            Log.e(TAG, "✗ Upload failed: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            e.printStackTrace()
            null
        } finally {
            Log.d(TAG, "=== UPLOAD IMAGE END ===")
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
                e.printStackTrace()
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
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    onError("User not authenticated")
                    return@launch
                }

                val draftData = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "postType" to postType.name,
                    "phone" to phone,
                    "date" to date,
                    "category" to category,
                    "images" to images.map { it.uri },
                    "questions" to questions.map {
                        mapOf("question" to it.question, "answer" to it.answer)
                    },
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("drafts")
                    .add(draftData)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save draft")
            }
        }
    }
}