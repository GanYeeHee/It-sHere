// file name: LoginViewModel.kt
package com.example.itshere.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Dao.AppDatabase
import com.example.itshere.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val auth = FirebaseAuth.getInstance()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun login(
        context: Context,
        onSuccess: () -> Unit,
        onEmailNotVerified: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentState = _state.value

        // Validation
        if (currentState.email.isBlank()) {
            _state.value = currentState.copy(emailError = "Email cannot be empty")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.value = currentState.copy(emailError = "Please enter a valid email")
            return
        }

        if (currentState.password.isBlank()) {
            _state.value = currentState.copy(passwordError = "Password cannot be empty")
            return
        }

        if (currentState.password.length < 6) {
            _state.value = currentState.copy(passwordError = "Password must be at least 6 characters")
            return
        }

        _state.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        _state.value = _state.value.copy(isLoading = false)

                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user?.isEmailVerified == true) {
                                // 获取用户信息
                                val userId = user.uid
                                val userEmail = user.email ?: currentState.email
                                val userName = user.displayName ?: "User"
                                val phoneNumber = user.phoneNumber
                                val isEmailVerified = user.isEmailVerified
                                val photoUrl = user.photoUrl?.toString()

                                println("✅ User logged in successfully:")
                                println("   UID: $userId")
                                println("   Email: $userEmail")
                                println("   Name: $userName")
                                println("   Email Verified: $isEmailVerified")

                                // 同步用户数据到本地数据库
                                viewModelScope.launch(Dispatchers.IO) {
                                    try {
                                        // 创建用户对象
                                        val userData = com.example.itshere.Data.User(
                                            uid = userId,
                                            email = userEmail,
                                            displayName = userName,
                                            phoneNumber = phoneNumber,
                                            photoUrl = photoUrl,
                                            isEmailVerified = isEmailVerified,
                                            createdAt = user.metadata?.creationTimestamp ?: System.currentTimeMillis(),
                                            lastSignInTime = System.currentTimeMillis(),
                                            providerId = user.providerId
                                        )

                                        // 获取数据库和仓库
                                        val database = AppDatabase.getInstance(context)
                                        val userRepository = UserRepository(database)

                                        // 保存到本地数据库
                                        database.userDao().insertUser(userData)
                                        println("✅ User saved to local database")

                                        // 同步到 Firestore
                                        userRepository.syncUserToFirestore(userData)
                                        println("✅ User synced to Firestore")

                                        // 验证数据是否保存成功
                                        val savedUser = database.userDao().getUserById(userId)
                                        if (savedUser != null) {
                                            println("✅ User data verified in local database:")
                                            println("   Saved UID: ${savedUser.uid}")
                                            println("   Saved Email: ${savedUser.email}")
                                            println("   Total users in DB: ${database.userDao().getAllUsers().size}")
                                        } else {
                                            println("❌ User not found in local database after save")
                                        }

                                    } catch (e: Exception) {
                                        println("❌ Error saving user to local database: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }

                                onSuccess()
                            } else {
                                println("❌ Email not verified for user: ${user?.email}")
                                auth.signOut()
                                onEmailNotVerified()
                            }
                        } else {
                            val errorMsg = task.exception?.message ?: "Login failed"
                            println("❌ Login failed: $errorMsg")
                            onError(errorMsg)
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                val errorMsg = e.message ?: "Login failed"
                println("❌ Login exception: $errorMsg")
                onError(errorMsg)
            }
        }
    }
}