package com.example.itshere.Data.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.itshere.Data.Entity.Admin

@Dao
interface AdminDao {
    // Inserts or replaces the single admin entry (using OnConflictStrategy.REPLACE ensures only one entry exists)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: Admin)

    // Retrieves the single admin entry
    @Query("SELECT * FROM admin WHERE adminId = 1")
    suspend fun getAdminCredentials(): Admin?

    // Optional utility to check if the table is empty
    @Query("SELECT COUNT(*) FROM admin")
    suspend fun getAdminCount(): Int
}