// 文件: Dao/LocalImage.kt
package com.example.itshere.Dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "local_images")
data class LocalImage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val postId: String = "",
    val uri: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUploaded: Boolean = false
)