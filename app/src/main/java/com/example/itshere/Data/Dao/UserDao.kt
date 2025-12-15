package com.example.itshere.Data.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.itshere.Data.Entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1")
    suspend fun getLastUserCode(): String?

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getById(userId: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM users")
    suspend fun clear()
}
