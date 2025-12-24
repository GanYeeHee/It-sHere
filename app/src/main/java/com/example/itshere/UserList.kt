package com.example.itshere

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.itshere.data.entity.User
import com.example.itshere.repository.UserRepository
import com.example.itshere.viewModel.UserListViewModel

@Composable
fun UserListScreen(
    navController: NavController,
    repository: UserRepository
) {
    val viewModel = remember {
        UserListViewModel(repository)
    }

    val users = viewModel.users

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp, start = 16.dp, end = 16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "User List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(users) { user ->
                UserRow(user) {
                    navController.navigate("userDetail/${user.userId}")
                }
            }
        }
    }
}

@Composable
fun UserRow(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${user.userId}", fontWeight = FontWeight.Bold)
            Text(text = "Name: ${user.name}")
        }
    }
}

@Composable
fun UserDetailScreen(
    navController: NavController,
    userId: String,
    repository: UserRepository
) {
    var user by remember { mutableStateOf<User?>(null) }

    // Load user from Room
    LaunchedEffect(userId) {
        user = repository.getUserById(userId)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 60.dp, start = 16.dp, end = 16.dp)
    ) {
        // Back button + Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        user?.let { u ->
            Text(text = "ID: ${u.userId}", fontWeight = FontWeight.Bold)
            Text(text = "Name: ${u.name}")
            Text(text = "Phone: ${u.phone}")
            Text(text = "Email: ${u.email}")
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}





