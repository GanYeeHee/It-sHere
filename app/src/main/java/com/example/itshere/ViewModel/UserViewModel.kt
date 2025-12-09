package com.example.itshere.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.User
import com.example.itshere.Repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserState(
    val currentUser: User? = null,
    val localUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    init {
        viewModelScope.launch {
            userRepository.getAllLocalUsersFlow()
                .collect { users ->
                    _state.value = _state.value.copy(localUsers = users)
                }
        }
    }

    fun syncCurrentUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                userRepository.syncCurrentUserToLocal()
                refreshCurrentUser()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to sync user: ${e.message}"
                )
            }
        }
    }

    private suspend fun refreshCurrentUser() {
        val user = userRepository.getCurrentUser()
        _state.value = _state.value.copy(currentUser = user)
    }

    fun syncUserFromFirestore(uid: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                userRepository.syncUserFromFirestore(uid)
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to sync from Firestore: ${e.message}"
                )
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                userRepository.updateUser(user)
                refreshCurrentUser()
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to update user: ${e.message}"
                )
            }
        }
    }

    fun clearLocalUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                userRepository.clearAllLocalUsers()
                _state.value = _state.value.copy(
                    isLoading = false,
                    localUsers = emptyList(),
                    currentUser = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to clear users: ${e.message}"
                )
            }
        }
    }
}