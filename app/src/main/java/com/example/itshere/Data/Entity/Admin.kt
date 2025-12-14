package com.example.itshere.Data.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Admin")
data class Admin(
    // We only ever expect one admin entry, so we use a constant primary key.
    @PrimaryKey
    val adminId: Int = 1,
    val email: String,
    val passwordHash: String
)