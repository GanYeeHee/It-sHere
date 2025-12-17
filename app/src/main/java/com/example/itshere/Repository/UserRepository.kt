package com.example.itshere.Repository

import com.example.itshere.Data.AppDatabase
import com.example.itshere.Data.Entity.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val database: AppDatabase
) {

    private val userDao = database.userDao()

    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }
    suspend fun deleteUser(user: User) {
        userDao.delete(user)
    }
    suspend fun getAllUsers(): List<User> {
        return userDao.getAll()
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getById(userId)
    }

    suspend fun getUserByFirebaseUid(firebaseUid: String): User? {
        return userDao.getByFirebaseUid(firebaseUid)
    }

    suspend fun clearUsers() {
        userDao.clear()
    }
}