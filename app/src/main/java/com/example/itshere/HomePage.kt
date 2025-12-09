package com.example.itshere

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.itshere.Data.PostData
import com.example.itshere.ViewModel.PostViewModel
import com.example.itshere.ViewModel.PostViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.io.File

@Composable
fun HomePage(
    onCreatePostClick: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel(
        factory = PostViewModelFactory.getFactory(context)
    )

    HomePageContent(
        navController = navController,
        viewModel = viewModel,
        onCreatePostClick = onCreatePostClick,
        onPostClick = onPostClick,
        onLogoutSuccess = onLogoutSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomePageContent(
    navController: NavController,
    viewModel: PostViewModel,
    onCreatePostClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hi,",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = currentUser?.displayName ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePostClick,
                containerColor = Color(0xFF7C4DFF),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Post"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.posts.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF7C4DFF)
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.loadPosts() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C4DFF)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                state.posts.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No posts yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Be the first to create a post!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.posts) { post ->
                            PostCardGrid(
                                post = post,
                                onFavoriteClick = { viewModel.toggleFavorite(post.id) },
                                onClick = { onPostClick(post.id) }
                            )
                        }
                    }
                }
            }

            if (showMenu) {
                SideMenu(
                    userDisplayName = currentUser?.displayName ?: "Name",
                    userPhone = currentUser?.phoneNumber ?: "+6012-3456789",
                    onSavedClick = {
                        // TODO: Navigate to saved posts
                        showMenu = false
                    },
                    onNotificationClick = {
                        navController.navigate("notifications")
                        showMenu = false
                    },
                    onAboutUsClick = {
                        navController.navigate("about_us")
                        showMenu = false
                    },
                    onSettingClick = {
                        navController.navigate("settings")
                        showMenu = false
                    },
                    onLogoutClick = {
                        showMenu = false
                        showLogoutDialog = true
                    },
                    onDismiss = { showMenu = false }
                )
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Text(
                            text = "Logout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        Text("Are you sure you want to logout?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                logoutUser(onLogoutSuccess)
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

private fun logoutUser(onLogoutSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.signOut()
    onLogoutSuccess()
}

@Composable
fun SideMenu(
    userDisplayName: String,
    userPhone: String,
    onSavedClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onSettingClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .align(Alignment.CenterEnd)
                .clickable { },
            color = Color.White,
            shape = RoundedCornerShape(bottomStart = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close menu",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "More",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column {
                    Text(
                        text = userDisplayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = userPhone,
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                MenuItem(
                    icon = Icons.Default.Favorite,
                    text = "Saved",
                    onClick = onSavedClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                MenuItem(
                    icon = Icons.Default.Notifications,
                    text = "Notification",
                    onClick = onNotificationClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                MenuItem(
                    icon = Icons.Default.Info,
                    text = "About Us",
                    onClick = onAboutUsClick
                )

                Spacer(modifier = Modifier.height(20.dp))

                MenuItem(
                    icon = Icons.Default.Settings,
                    text = "Setting",
                    onClick = onSettingClick
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFCDD2)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFF666666),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 18.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

private fun logoutUser() {
    val auth = FirebaseAuth.getInstance()
    auth.signOut()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCardGrid(
    post: PostData,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val timeAgo = remember(post.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - post.timestamp
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (post.imageUrls.isNotEmpty()) {
                    val imagePath = post.imageUrls.first()
                    val imageFile = File(imagePath)

                    if (imageFile.exists() && imageFile.canRead()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageFile),
                            contentDescription = "Post image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFFCDD2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "File not found",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "File not found",
                                    color = Color(0xFFC62828),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF87CEEB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No image",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = if (post.postType == "FOUND") Color(0xFFE1BEE7) else Color(0xFFBBDEFB),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = post.postType,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (post.postType == "FOUND") Color(0xFF6A1B9A) else Color(0xFF1976D2)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (post.isFavorite) Color.Red else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}