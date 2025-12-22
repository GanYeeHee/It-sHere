package com.example.itshere.Data

data class ClaimRequest(
    val id: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val requesterId: String = "",   // Must have = ""
    val requesterName: String = "", // Must have = ""
    val answers: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)