package com.example.itshere.Data

import java.util.UUID

data class PostData(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val postType: String = "FOUND",
    val phone: String = "",
    val date: String = "",
    val category: String = "",
    val imageUrls: List<String> = emptyList(),
    val questions: List<Map<String, String>> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)