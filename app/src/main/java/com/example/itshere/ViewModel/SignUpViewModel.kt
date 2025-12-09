package com.example.itshere.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.User
import com.example.itshere.Dao.AppDatabase
import com.example.itshere.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SignUpState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class SignUpViewModel : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state

    private val auth = FirebaseAuth.getInstance()

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(
            name = name,
            nameError = null
        )
    }

    fun onPhoneChange(phone: String) {
        _state.value = _state.value.copy(
            phone = phone,
            phoneError = null
        )
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null,
            confirmPasswordError = null
        )
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun signUp(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentState = _state.value

        // Validation
        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(nameError = "Name cannot be empty")
            return
        }

        if (currentState.phone.isBlank()) {
            _state.value = currentState.copy(phoneError = "Phone number cannot be empty")
            return
        }

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

        if (currentState.confirmPassword.isBlank()) {
            _state.value = currentState.copy(confirmPasswordError = "Please confirm your password")
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.value = currentState.copy(confirmPasswordError = "Passwords do not match")
            return
        }

        _state.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid ?: ""

                            // Update profile with name
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(currentState.name)
                                .build()

                            user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // 创建用户数据对象
                                    val userData = User(
                                        uid = userId,
                                        email = currentState.email,
                                        displayName = currentState.name,
                                        phoneNumber = currentState.phone,
                                        isEmailVerified = false,
                                        createdAt = System.currentTimeMillis(),
                                        lastSignInTime = System.currentTimeMillis(),
                                        providerId = "password"
                                    )

                                    // 保存到本地数据库和 Firestore
                                    viewModelScope.launch(Dispatchers.IO) {
                                        try {
                                            // 获取数据库和仓库
                                            val database = AppDatabase.getInstance(context)
                                            val userRepository = UserRepository(database)

                                            // 保存到本地数据库
                                            database.userDao().insertUser(userData)

                                            // 同步到 Firestore
                                            userRepository.syncUserToFirestore(userData)

                                            println("✅ User saved to local database and Firestore:")
                                            println("   UID: $userId")
                                            println("   Email: ${currentState.email}")
                                            println("   Name: ${currentState.name}")
                                        } catch (e: Exception) {
                                            println("❌ Error saving user: ${e.message}")
                                            e.printStackTrace()
                                        }
                                    }

                                    // Send verification email
                                    user.sendEmailVerification()
                                        .addOnCompleteListener { emailTask ->
                                            _state.value = _state.value.copy(isLoading = false)
                                            if (emailTask.isSuccessful) {
                                                println("✅ Verification email sent to ${currentState.email}")
                                                onSuccess()
                                            } else {
                                                val errorMsg = "Account created but failed to send verification email"
                                                println("❌ $errorMsg")
                                                onError(errorMsg)
                                            }
                                        }
                                } else {
                                    _state.value = _state.value.copy(isLoading = false)
                                    val errorMsg = "Failed to update profile: ${updateTask.exception?.message}"
                                    println("❌ $errorMsg")
                                    onError(errorMsg)
                                }
                            }
                        } else {
                            _state.value = _state.value.copy(isLoading = false)
                            val errorMsg = task.exception?.message ?: "Sign up failed"
                            println("❌ Sign up failed: $errorMsg")
                            onError(errorMsg)
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                val errorMsg = e.message ?: "Sign up failed"
                println("❌ Sign up exception: $errorMsg")
                onError(errorMsg)
            }
        }
    }
}