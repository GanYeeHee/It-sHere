package com.example.itshere.Repository

import androidx.lifecycle.ViewModel
import com.example.itshere.Data.ClaimRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Create a new file: RequestRepository.kt
object RequestRepository {
    private val _requests = MutableStateFlow<List<ClaimRequest>>(emptyList())
    val requests = _requests.asStateFlow()
    fun addRequest(request: ClaimRequest) {
        _requests.value = _requests.value + request
    }
}

// Then in your ViewModel:
class PostViewModel : ViewModel() {
    val requests = RequestRepository.requests

    fun submitClaimRequest(postId: String, postTitle: String, answers: List<String>) {
        val newRequest = ClaimRequest(postId = postId, postTitle = postTitle, answers = answers)
        RequestRepository.addRequest(newRequest) // Saves to the global object
    }
}