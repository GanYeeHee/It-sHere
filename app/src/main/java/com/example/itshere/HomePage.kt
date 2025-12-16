// file name: HomePage.kt
package com.example.itshere

import android.app.DatePickerDialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.itshere.Data.AppDatabase
import com.example.itshere.Data.PostData
import com.example.itshere.viewModel.PostViewModel
import com.example.itshere.viewModel.PostViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

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
    val context = LocalContext.current

    // State for storing user phone number from database
    var userPhoneFromDb by remember { mutableStateOf("") }
    var userNameFromDb by remember { mutableStateOf("") }

    // Load user data from Room database when user changes
    LaunchedEffect(currentUser?.uid) {
        if (currentUser?.uid != null) {
            withContext(Dispatchers.IO) {
                val database = AppDatabase.getInstance(context)
                val user = database.userDao().getByFirebaseUid(currentUser.uid)
                user?.let {
                    userPhoneFromDb = it.phone
                    userNameFromDb = it.name
                }
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    // Date filter states
    var showDateFilterDialog by remember { mutableStateOf(false) }
    var selectedDateOption by remember { mutableStateOf("All") } // "All" or "Custom"
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }

    // Available categories
    val categories = remember {
        listOf("All", "Electronic", "Clothes", "Cards", "Accessories", "Documents", "Others")
    }

    // Determine what to display in the drawer
    val displayName = if (userNameFromDb.isNotEmpty()) userNameFromDb else currentUser?.displayName ?: "User"
    val displayPhone = if (userPhoneFromDb.isNotEmpty()) userPhoneFromDb else currentUser?.phoneNumber ?: "Phone not set"

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerContent(
                userDisplayName = displayName,
                userPhone = displayPhone,
                onSavedClick = { scope.launch { drawerState.close() }
                    navController.navigate("saved")
                               },
                onNotificationClick = {
                    navController.navigate("notifications")
                    scope.launch { drawerState.close() }
                },
                onAboutUsClick = {
                    navController.navigate("about_us")
                    scope.launch { drawerState.close() }
                },
                onSettingClick = {
                    navController.navigate("settings")
                    scope.launch { drawerState.close() }
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() } // Close drawer first
                    showLogoutDialog = true
                }
            )
        }
    ) {
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
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search Bar Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f) // Take all space except trailing icon
                            .height(52.dp),
                        placeholder = { Text("Search posts") },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C4DFF),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF7C4DFF)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDateFilterDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Date filter",
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C4DFF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF5F5F5),
                                labelColor = Color.Gray
                            )
                        )
                    }
                }

                if (showDateFilterDialog) {
                    DateFilterDialog(
                        showDialog = true,
                        selectedOption = selectedDateOption,
                        onOptionChange = { selectedDateOption = it },
                        startDate = customStartDate,
                        endDate = customEndDate,
                        onStartDateChange = { customStartDate = it },
                        onEndDateChange = { customEndDate = it },
                        onConfirm = {
                            // TODO: Apply filter logic
                            showDateFilterDialog = false
                        },
                        onDismiss = { showDateFilterDialog = false }
                    )
                }

                // Filter posts based on search query, category, and date
                val filteredPosts = remember(state.posts, searchQuery, selectedCategory, selectedDateOption, customStartDate, customEndDate) {
                    val lowercaseQuery = searchQuery.lowercase()

                    val searchedPosts = if (lowercaseQuery.isEmpty()) {
                        state.posts
                    } else {
                        state.posts.filter { post ->
                            post.title.lowercase().contains(lowercaseQuery) ||
                                    post.description?.lowercase()?.contains(lowercaseQuery) == true ||
                                    post.category.lowercase().contains(lowercaseQuery) ||
                                    post.postType.lowercase().contains(lowercaseQuery)
                        }
                    }

                    val categoryFiltered = if (selectedCategory == "All") {
                        searchedPosts
                    } else {
                        searchedPosts.filter { post ->
                            post.category.contains(selectedCategory, ignoreCase = true)
                        }
                    }

                    // Filter by date if Custom selected
                    if (selectedDateOption == "Custom" && customStartDate.isNotEmpty() && customEndDate.isNotEmpty()) {
                        categoryFiltered.filter { post ->
                            val postDate = post.timestampToDateString()
                            postDate >= customStartDate && postDate <= customEndDate
                        }
                    } else {
                        categoryFiltered
                    }
                }

                // Posts Grid
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
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
                        filteredPosts.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = "No results",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No matching posts",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Try changing your search or filter",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                } else {
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
                        }
                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredPosts) { post ->
                                    PostCardGrid(
                                        post = post,
                                        onFavoriteClick = { viewModel.toggleFavorite(post.id) },
                                        onClick = { onPostClick(post.id) }
                                    )
                                }
                            }
                        }
                    }

                    // Logout Dialog
                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            title = { Text("Logout", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                            text = { Text("Are you sure you want to logout?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showLogoutDialog = false
                                        logoutUser(onLogoutSuccess)
                                    }
                                ) { Text("Yes") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) { Text("No") }
                            }
                        )
                    }
                }
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
fun DrawerContent(
    userDisplayName: String,
    userPhone: String,
    onSavedClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onSettingClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp) // Set width instead of fillMax
    ) {
        // Drawer Header (User Info)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0)) // Light gray background for header
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Placeholder for User Avatar/Icon (Optional)
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "User Avatar",
                tint = Color(0xFF7C4DFF),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userDisplayName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userPhone,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Navigation Items
        Column(
            modifier = Modifier
                .weight(1f) // Take up remaining space
                .padding(vertical = 8.dp)
        ) {
            NavigationDrawerItem(
                label = { Text("Saved") },
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Saved") },
                selected = false, // Add logic if you want to highlight the current screen
                onClick = onSavedClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Notification") },
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Notification") },
                selected = false,
                onClick = onNotificationClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("About Us") },
                icon = { Icon(Icons.Default.Info, contentDescription = "About Us") },
                selected = false,
                onClick = onAboutUsClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Setting") },
                selected = false,
                onClick = onSettingClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // Logout Button at the bottom
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            NavigationDrawerItem(
                label = { Text("Logout", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Logout") },
                selected = false,
                onClick = onLogoutClick,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = Color.Red,
                    unselectedIconColor = Color.Red
                ),
                modifier = Modifier.fillMaxWidth()
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterDialog(
    showDialog: Boolean,
    selectedOption: String,
    onOptionChange: (String) -> Unit,
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Date") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == "Custom",
                        onClick = { onOptionChange("Custom") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Custom Range")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedOption == "All",
                        onClick = { onOptionChange("All") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All")
                }

                if (selectedOption == "Custom") {
                    Spacer(modifier = Modifier.height(16.dp))

                    val context = LocalContext.current
                    // Start Date Picker
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Date") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val today = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        onStartDateChange("$year-${month + 1}-$dayOfMonth")
                                    },
                                    today.get(Calendar.YEAR),
                                    today.get(Calendar.MONTH),
                                    today.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick Start Date")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // End Date Picker
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Date") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                val today = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        onEndDateChange("$year-${month + 1}-$dayOfMonth")
                                    },
                                    today.get(Calendar.YEAR),
                                    today.get(Calendar.MONTH),
                                    today.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick End Date")
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedOption == "All" ||
                        (startDate.isNotEmpty() && endDate.isNotEmpty() && startDate <= endDate)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun PostData.timestampToDateString(): String {
    val cal = Calendar.getInstance().apply { timeInMillis = this@timestampToDateString.timestamp }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1 // Months are 0-indexed
    val day = cal.get(Calendar.DAY_OF_MONTH)
    // Format as "YYYY-MM-DD" so it can be compared lexicographically
    return "%04d-%02d-%02d".format(year, month, day)
}

@Preview(showBackground = true)
@Composable
fun PreDrawerContent() {
    DrawerContent(
        onLogoutClick = {},
        onSavedClick = {},
        onNotificationClick = {},
        onAboutUsClick = {},
        onSettingClick = {},
        userPhone = "+6012-3456789",
        userDisplayName = "Preview User"
    )
}