package com.example.itshere.viewModel

import android.content.Context
import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.Entity.User
import com.example.itshere.Data.AppDatabase
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
    private fun generateNextUserCode(lastCode: String?): String {
        return if (lastCode == null) "U0001"
        else {
            val number = lastCode.substring(1).toInt()
            "U" + String.format("%04d", number + 1)
        }
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

                auth.createUserWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            val firebaseUid = firebaseUser?.uid ?: ""

                            if (firebaseUser == null) {
                                _state.value = _state.value.copy(isLoading = false)
                                onError("User creation failed")
                                return@addOnCompleteListener
                            }


                            viewModelScope.launch(Dispatchers.IO) {
                                val userDao = AppDatabase.getInstance(context).userDao()

                                val lastCode = userDao.getLastUserCode()   // you need to add this DAO method
                                val newUserCode = generateNextUserCode(lastCode)

                                val userEntity = User(
                                    firebaseUid = firebaseUid,
                                    userId = newUserCode,
                                    name = currentState.name,
                                    phone = currentState.phone,
                                    email = currentState.email,
                                    password = currentState.password
                                )

                                userDao.insert(userEntity)
                            }

                            // Optional: email verification
                            firebaseUser.sendEmailVerification()

                            _state.value = _state.value.copy(isLoading = false)
                            onSuccess()
                        }else {
                            _state.value = _state.value.copy(isLoading = false)
                            val errorMsg = task.exception?.message ?: "Sign up failed"
                            onError(errorMsg)
                        }
                    }
        }
    }
}