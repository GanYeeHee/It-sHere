package com.example.itshere.viewModel

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
