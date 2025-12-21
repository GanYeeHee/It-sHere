package com.example.itshere.Data

data class ClaimRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val postId: String,
    val postTitle: String,
    val requesterName: String = "Anonymous User", // Add this line
    val answers: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)