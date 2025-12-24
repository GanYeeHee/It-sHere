package com.example.itshere.data

data class ClaimRequest(
    val id: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val answers: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)