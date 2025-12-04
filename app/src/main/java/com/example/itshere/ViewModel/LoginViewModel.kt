// LoginViewModel.kt
package com.example.itshere.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
                                onSuccess()
                            } else {
                                auth.signOut()
                                onEmailNotVerified()
                            }
                        } else {
                            onError(task.exception?.message ?: "Login failed")
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                onError(e.message ?: "Login failed")
            }
        }
    }
}