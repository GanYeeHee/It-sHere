package com.example.itshere

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.itshere.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: UserViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        // 自动同步当前用户
        viewModel.syncCurrentUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "User Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.syncCurrentUser() }) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color(0xFF824DFF)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearLocalUsers() },
                        enabled = state.localUsers.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = if (state.localUsers.isNotEmpty()) Color.Red else Color.Gray
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF824DFF))
                            Text("Syncing user data...")
                        }
                    }
                } else {
                    // Current User Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "👤 Current User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF824DFF)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            state.currentUser?.let { user ->
                                UserDetailRow(label = "User ID", value = user.uid)
                                UserDetailRow(label = "Email", value = user.email)
                                UserDetailRow(label = "Name", value = user.displayName ?: "Not set")
                                UserDetailRow(label = "Phone", value = user.phoneNumber ?: "Not set")
                                UserDetailRow(
                                    label = "Email Verified",
                                    value = if (user.isEmailVerified) "✅ Verified" else "❌ Not Verified",
                                    valueColor = if (user.isEmailVerified) Color.Green else Color.Red
                                )
                                UserDetailRow(label = "Provider", value = user.providerId)
                                UserDetailRow(label = "Created", value = formatTimestamp(user.createdAt))
                                UserDetailRow(label = "Last Login", value = formatTimestamp(user.lastSignInTime))
                            } ?: run {
                                Text(
                                    text = "No user is currently logged in",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Local Database Users Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Local Database Users (${state.localUsers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF824DFF)
                        )

                        if (state.localUsers.isNotEmpty()) {
                            Badge(
                                containerColor = Color(0xFF824DFF),
                                contentColor = Color.White
                            ) {
                                Text(state.localUsers.size.toString())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.localUsers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonOutline,
                                    contentDescription = "No users",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Text(
                                    text = "No users in local database",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Button(
                                    onClick = { viewModel.syncCurrentUser() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF824DFF)
                                    )
                                ) {
                                    Text("Sync Current User")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.localUsers) { user ->
                                UserCard(user = user)
                            }
                        }
                    }
                }
            }

            if (state.error != null) {
                AlertDialog(
                    onDismissRequest = { /* Can't dismiss */ },
                    title = {
                        Text(
                            "Error",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(state.error!!)
                    },
                    confirmButton = {
                        Button(
                            onClick = { /* Handle error */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF824DFF)
                            )
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun UserCard(user: com.example.itshere.Data.User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF824DFF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = user.displayName ?: user.email.split("@")[0],
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (user.isEmailVerified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            UserDetailRow(label = "Email", value = user.email)
            user.phoneNumber?.let {
                UserDetailRow(label = "Phone", value = it)
            }
            UserDetailRow(
                label = "UID",
                value = user.uid.take(12) + "...",
                valueColor = Color(0xFF666666)
            )
            UserDetailRow(label = "Created", value = formatTimestamp(user.createdAt))

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // User actions
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser?.uid == user.uid) {
                    Badge(
                        containerColor = Color(0xFF824DFF),
                        contentColor = Color.White
                    ) {
                        Text("Current")
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        sdf.format(date)
    } catch (e: Exception) {
        "Invalid date"
    }
}