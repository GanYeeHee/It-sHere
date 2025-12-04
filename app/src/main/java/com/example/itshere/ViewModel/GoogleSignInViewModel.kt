package com.example.itshere.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class GoogleSignInState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class GoogleSignInViewModel : ViewModel() {
    private val _state = MutableStateFlow(GoogleSignInState())
    val state: StateFlow<GoogleSignInState> = _state

    private val auth = FirebaseAuth.getInstance()

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()

                _state.value = _state.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    errorMessage = null
                )
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Google sign in failed"
                )
                onError(e.message ?: "Google sign in failed")
            }
        }
    }

    fun resetState() {
        _state.value = GoogleSignInState()
    }
}