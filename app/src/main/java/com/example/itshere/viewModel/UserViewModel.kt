package com.example.itshere.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.Entity.User
import com.example.itshere.Repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class UserState(
    val users: List<User> = emptyList()
)

class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state
    fun loadUsers() {
        viewModelScope.launch {
            val users = userRepository.getAllUsers()
            _state.value = UserState(users)
        }
    }
}

class UserListViewModel(
    private val repository: UserRepository
) : ViewModel() {

    var users by mutableStateOf<List<User>>(emptyList())
        private set
    init {
        viewModelScope.launch {
            users = repository.getAllUsers()
        }
    }
}
