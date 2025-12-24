package com.example.itshere.repository

import com.example.itshere.data.ClaimRequest
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

