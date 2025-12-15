package com.example.itshere.Data.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String,
    @ColumnInfo(name = "user_id")
    val userId: String,

    val name: String,

    val phone: String,

    val email: String,

    val password: String
)