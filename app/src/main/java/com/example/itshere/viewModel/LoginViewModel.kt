// file name: LoginViewModel.kt
package com.example.itshere.viewModel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.itshere.Data.Entity.Admin
import com.example.itshere.Repository.UserRepository

// --- CONSTANTS FOR INITIAL SEEDING & CHECKING ---
private const val INITIAL_ADMIN_EMAIL = "admin25@itshere.com"
private const val INITIAL_ADMIN_PASSWORD = "itsMin!pro25" // The actual password for login check
private const val INITIAL_ADMIN_ID = "ADMIN25" // The unique ID stored in the database

// Masked value for display in App Inspection:
private const val INITIAL_ADMIN_PASSWORD_MASKED = "its*******"
// ------------------------------------------------


data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

// CLASS SIGNATURE CHANGED to AndroidViewModel
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    private val auth = FirebaseAuth.getInstance()

    // DAO INSTANCE
    private val adminDao = AppDatabase.getInstance(application).adminDao()

    // INITIALIZATION BLOCK - SEEDING DATA
    init {
        seedAdminCredentials()
    }

    // SEEDING FUNCTION
    private fun seedAdminCredentials() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Check if the Admin table is empty (we check by trying to retrieve the ID)
                if (adminDao.getAdminCredentials() == null) {
                    val initialAdmin = Admin(
                        // ADMIN ID IS NOW A STRING, matching your request: "ADMIN25"
                        adminId = INITIAL_ADMIN_ID, // Keep Int as primary key, but use the unique fields
                        email = INITIAL_ADMIN_EMAIL,
                        password = INITIAL_ADMIN_PASSWORD_MASKED // Storing the masked string
                    )
                    adminDao.insertAdmin(initialAdmin)
                    println("✅ Admin credentials INSERTED into Room. ID: ADMIN25, Password: masked.")
                } else {
                    println("Admin credentials already exist in Room.")
                }
            } catch (e: Exception) {
                println("❌ Error seeding admin data: ${e.message}")
            }
        }
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

        // Validation (Existing code)
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

            // --- 1. ADMIN ROOM CREDENTIAL CHECK (RUNS FIRST) ---
            val enteredEmail = currentState.email
            val enteredPassword = currentState.password

            try {
                val roomAdmin = adminDao.getAdminCredentials()

                if (roomAdmin != null &&
                    enteredEmail == roomAdmin.email &&
                    enteredPassword == INITIAL_ADMIN_PASSWORD) { // <-- Checks against the ACTUAL password

                    // Admin Login Success
                    println("✅ Admin logged in successfully from Room check.")
                    _state.value = currentState.copy(isLoading = false)

                    // FIXED: Launching onSuccess on the Main Dispatcher safely
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess()
                    }
                    return@launch // EXIT
                }
            } catch (e: Exception) {
                println("❌ Error during Room Admin check: ${e.message}")
            }
            // --- END ADMIN ROOM CHECK ---


            // --- 2. EXISTING FIREBASE LOGIN (FALLBACK) ---
            try {
                auth.signInWithEmailAndPassword(currentState.email, currentState.password)
                    .addOnCompleteListener { task ->
                        _state.value = _state.value.copy(isLoading = false)

                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser

                            if (firebaseUser?.isEmailVerified == true) {
                                _state.value = currentState.copy(isLoading = false)

                                // ✅ Just navigate, NO database write
                                viewModelScope.launch(Dispatchers.Main) {
                                    onSuccess()
                                }

                            } else {
                                auth.signOut()
                                _state.value = currentState.copy(isLoading = false)
                                onEmailNotVerified()
                            }

                        } else {
                            _state.value = currentState.copy(isLoading = false)
                            val errorMsg = task.exception?.message ?: "Login failed"
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

    fun prefillCredentials(email: String, password: String) {
        _state.update {
            it.copy(
                email = email,
                password = password
            )
        }
    }

    fun clearForm() {
        _state.update {
            it.copy(
                email = "",
                password = "",
                emailError = null,
                passwordError = null
            )
        }
    }
}