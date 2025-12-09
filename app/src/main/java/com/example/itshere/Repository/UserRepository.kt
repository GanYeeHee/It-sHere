// file name: UserRepository.kt
package com.example.itshere.Repository

import com.example.itshere.Data.User
import com.example.itshere.Dao.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val appDatabase: AppDatabase
) {
    private val userDao = appDatabase.userDao()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 同步当前用户到本地数据库
    suspend fun syncCurrentUserToLocal() {
        val firebaseUser = auth.currentUser ?: return

        // 创建用户对象
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            phoneNumber = firebaseUser.phoneNumber,
            photoUrl = firebaseUser.photoUrl?.toString(),
            isEmailVerified = firebaseUser.isEmailVerified,
            createdAt = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastSignInTime = firebaseUser.metadata?.lastSignInTimestamp ?: System.currentTimeMillis(),
            providerId = firebaseUser.providerId,
            isAnonymous = firebaseUser.isAnonymous
        )

        // 保存到本地数据库
        userDao.insertUser(user)

        // 同步到 Firestore
        syncUserToFirestore(user)
    }

    // 同步用户到 Firestore
    suspend fun syncUserToFirestore(user: User) {
        try {
            firestore.collection("users")
                .document(user.uid)
                .set(
                    mapOf(
                        "uid" to user.uid,
                        "email" to user.email,
                        "displayName" to user.displayName,
                        "phoneNumber" to user.phoneNumber,
                        "photoUrl" to user.photoUrl,
                        "isEmailVerified" to user.isEmailVerified,
                        "createdAt" to user.createdAt,
                        "lastSignInTime" to user.lastSignInTime,
                        "providerId" to user.providerId,
                        "isAnonymous" to user.isAnonymous
                    )
                )
                .await()
        } catch (e: Exception) {
            // Firestore 同步失败，但本地数据库已保存
            e.printStackTrace()
        }
    }

    // 从 Firestore 获取用户并保存到本地
    suspend fun syncUserFromFirestore(uid: String) {
        try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            if (document.exists()) {
                val firestoreUser = document.toObject(User::class.java)
                firestoreUser?.let {
                    userDao.insertUser(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 获取本地所有用户
    suspend fun getAllLocalUsers(): List<User> {
        return userDao.getAllUsers()
    }

    // 获取本地用户流
    fun getAllLocalUsersFlow(): Flow<List<User>> {
        return userDao.getAllUsersFlow()
    }

    // 获取当前用户
    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return userDao.getUserById(uid)
    }

    // 更新用户信息
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        syncUserToFirestore(user)
    }

    // 清除所有本地用户数据
    suspend fun clearAllLocalUsers() {
        userDao.deleteAllUsers()
    }
}