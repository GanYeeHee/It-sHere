package com.example.itshere.Data.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.itshere.Data.Entity.Admin

@Dao
interface AdminDao {
    // Inserts the admin credentials. If an entry with adminId=1 already exists, it replaces it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: Admin)

    // Retrieves the single admin entry (which will always have ID 1)
    @Query("SELECT * FROM Admin WHERE adminId = 1")
    suspend fun getAdminCredentials(): Admin?

    // Optional: for verification during setup
    @Query("SELECT COUNT(*) FROM Admin")
    suspend fun getAdminCount(): Int
}