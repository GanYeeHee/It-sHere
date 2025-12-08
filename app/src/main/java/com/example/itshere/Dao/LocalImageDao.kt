package com.example.itshere.Dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalImageDao {
    @Insert
    suspend fun insert(image: LocalImage)

    @Insert
    suspend fun insertAll(images: List<LocalImage>)

    @Update
    suspend fun update(image: LocalImage)

    @Query("SELECT * FROM local_images WHERE postId = :postId ORDER BY timestamp ASC")
    fun getImagesByPostId(postId: String): Flow<List<LocalImage>>

    @Query("SELECT * FROM local_images WHERE postId = :postId AND isUploaded = 0")
    fun getUnuploadedImagesByPostId(postId: String): Flow<List<LocalImage>>

    @Query("SELECT * FROM local_images WHERE isUploaded = 0")
    fun getAllUnuploadedImages(): Flow<List<LocalImage>>

    @Query("DELETE FROM local_images WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM local_images WHERE postId = :postId")
    suspend fun deleteByPostId(postId: String)

    @Query("UPDATE local_images SET isUploaded = 1 WHERE id = :imageId")
    suspend fun markAsUploaded(imageId: String)

    @Query("SELECT COUNT(*) FROM local_images WHERE postId = :postId")
    suspend fun getImageCountByPostId(postId: String): Int
}