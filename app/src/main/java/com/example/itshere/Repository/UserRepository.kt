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

    suspend fun syncCurrentUserToLocal() {
        val firebaseUser = auth.currentUser ?: return

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

        userDao.insertUser(user)

        syncUserToFirestore(user)
    }

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
            e.printStackTrace()
        }
    }

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

    suspend fun getAllLocalUsers(): List<User> {
        return userDao.getAllUsers()
    }

    fun getAllLocalUsersFlow(): Flow<List<User>> {
        return userDao.getAllUsersFlow()
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return userDao.getUserById(uid)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        syncUserToFirestore(user)
    }

    suspend fun clearAllLocalUsers() {
        userDao.deleteAllUsers()
    }
}