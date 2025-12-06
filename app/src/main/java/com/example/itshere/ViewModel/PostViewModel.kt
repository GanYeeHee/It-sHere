package com.example.itshere.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.ImageItem
import com.example.itshere.Data.PostType
import com.example.itshere.Data.QuestionAnswer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
            _state.value = _state.value.copy(isLoading = true)

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    onError("User not authenticated")
                    _state.value = _state.value.copy(isLoading = false)
                    return@launch
                }

                // Upload images to Firebase Storage
                val imageUrls = mutableListOf<String>()
                for (imageItem in images) {
                    val imageUrl = uploadImage(imageItem.uri)
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl)
                    }
                }

                // Prepare questions data
                val questionsData = questions.map { qa ->
                    mapOf(
                        "question" to qa.question,
                        "answer" to qa.answer
                    )
                }

                // Create post document
                val postData = hashMapOf(
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: "Unknown User"),
                    "title" to title,
                    "description" to description,
                    "postType" to postType.name,
                    "phone" to phone,
                    "date" to date,
                    "category" to category,
                    "imageUrls" to imageUrls,
                    "questions" to questionsData,
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("posts")
                    .add(postData)
                    .await()

                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                onError(e.message ?: "Failed to create post")
            }
        }
    }

    private suspend fun uploadImage(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val fileName = "posts/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            storageRef.putFile(uri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
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