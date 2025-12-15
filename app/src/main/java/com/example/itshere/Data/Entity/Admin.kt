package com.example.itshere.Data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin")
data class Admin(
    // 1. Admin ID: Used as the unique key. We set a default of 1.
    @PrimaryKey
    val adminId: String,

    // 2. Admin Email
    val email: String,

    // 3. Password (stored locally as a string)
    val password: String
)