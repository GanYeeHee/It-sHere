package com.example.itshere

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.itshere.data.AppDatabase
import com.example.itshere.data.entity.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val notificationDao = database.notificationDao()

    val notifications by notificationDao.getAllNotifications()
        .collectAsState(initial = emptyList())
    val unreadCount by notificationDao.getUnreadCount()
        .collectAsState(initial = 0)

    val scope = rememberCoroutineScope()
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showDeleteReadDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        // Mark all as read button
                        if (unreadCount > 0) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            notificationDao.markAllAsRead()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Mark all as read",
                                    tint = Color(0xFF824DFF)
                                )
                            }
                        }

                        // More options menu
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color(0xFF666666)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteSweep,
                                                contentDescription = null,
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Delete All")
                                        }
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showDeleteAllDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color(0xFF666666),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Delete Read")
                                        }
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        showDeleteReadDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            if (notifications.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "No notifications",
                        tint = Color(0xFFCCCCCC),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You'll see notifications here when new posts are created",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Notification list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = notifications,
                        key = { notification -> notification.id }
                    ) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        notificationDao.markAsRead(notification.id)
                                    }
                                }
                                navController.navigate("post_details/${notification.postId}")
                            },
                            onDelete = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        notificationDao.deleteById(notification.id)
                                    }
                                }
                            },
                            onMarkUnread = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        notificationDao.markAsUnread(notification.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete",
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete All Notifications?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will permanently delete all ${notifications.size} notifications. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                notificationDao.deleteAll()
                            }
                        }
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel", color = Color(0xFF666666))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete read confirmation dialog
    if (showDeleteReadDialog) {
        val readCount = notifications.count { it.isRead }

        AlertDialog(
            onDismissRequest = { showDeleteReadDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Read Notifications?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will permanently delete $readCount read notifications. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                notificationDao.deleteAllRead()
                            }
                        }
                        showDeleteReadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF666666)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteReadDialog = false }) {
                    Text("Cancel", color = Color(0xFF666666))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkUnread: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val timeAgo = remember(notification.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - notification.timestamp
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            diff < 604800000 -> "${diff / 86400000}d ago"
            else -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                sdf.format(Date(notification.timestamp))
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color.White else Color(0xFFF3E5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification icon
            Surface(
                shape = CircleShape,
                color = if (notification.postType == "FOUND")
                    Color(0xFFE1BEE7)
                else
                    Color(0xFFBBDEFB),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (notification.postType == "FOUND")
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Search,
                        contentDescription = notification.postType,
                        tint = if (notification.postType == "FOUND")
                            Color(0xFF6A1B9A)
                        else
                            Color(0xFF1976D2),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Notification content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!notification.isRead) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF824DFF),
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }

                    Text(
                        text = "New ${notification.postType.lowercase()} post",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (notification.postType == "FOUND")
                            Color(0xFF6A1B9A)
                        else
                            Color(0xFF1976D2)
                    )
                }

                Text(
                    text = notification.postTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFE0B2),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = notification.postCategory,
                            fontSize = 11.sp,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = timeAgo,
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            // Menu button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (notification.isRead) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailUnread,
                                        contentDescription = null,
                                        tint = Color(0xFF824DFF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Mark as unread")
                                }
                            },
                            onClick = {
                                showMenu = false
                                onMarkUnread()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Delete", color = Color.Red)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreNotificationsScreen() {
    NotificationsScreen()
}

@Preview(showBackground = true)
@Composable
fun PreNotificationCard() {
    val sampleNotification = Notification(
        id = "1",
        postId = "post1",
        postTitle = "Found Blue Backpack at Library",
        postType = "FOUND",
        postCategory = "Bag",
        timestamp = System.currentTimeMillis() - 3600000, // 1 hour ago
        isRead = false
    )

    NotificationCard(
        notification = sampleNotification,
        onClick = {},
        onDelete = {},
        onMarkUnread = {}
    )
}