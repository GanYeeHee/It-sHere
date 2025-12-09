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
        // 监听本地用户变化
        viewModelScope.launch {
            userRepository.getAllLocalUsersFlow()
                .collect { users ->
                    _state.value = _state.value.copy(localUsers = users)
                }
        }
    }

    // 同步当前用户
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

    // 刷新当前用户
    private suspend fun refreshCurrentUser() {
        val user = userRepository.getCurrentUser()
        _state.value = _state.value.copy(currentUser = user)
    }

    // 从 Firestore 同步特定用户
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

    // 更新用户信息
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

    // 清除本地用户数据
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