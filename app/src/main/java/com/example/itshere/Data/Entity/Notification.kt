package com.example.itshere.Data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val postTitle: String,
    val postType: String, // LOST or FOUND
    val postCategory: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)