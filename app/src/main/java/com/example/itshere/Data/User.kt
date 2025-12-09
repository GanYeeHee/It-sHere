package com.example.itshere.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInTime: Long = System.currentTimeMillis(),
    val providerId: String = "firebase",
    val isAnonymous: Boolean = false,
    val isNewUser: Boolean = true
)