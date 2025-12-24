package com.example.itshere.Data

data class ClaimRequest(
    val id: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val answers: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)