package com.example.itshere.repository

import com.example.itshere.data.AppDatabase
import com.example.itshere.data.entity.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val userDao = database.userDao()

    suspend fun getAllUsers(): List<User> {
        return userDao.getAll()
    }
    suspend fun getUserById(userId: String): User? {
        return userDao.getById(userId)
    }
    suspend fun getUserByFirebaseUid(firebaseUid: String): User? {
        return userDao.getByFirebaseUid(firebaseUid)
    }

}