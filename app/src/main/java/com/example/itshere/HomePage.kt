package com.example.itshere

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.itshere.Data.PostData
import com.example.itshere.ViewModel.PostViewModel
import com.example.itshere.ViewModel.PostViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomePage(
    onCreatePostClick: () -> Unit = {},
    onPostClick: (String) -> Unit = {},
    onViewDraftsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel(
        factory = PostViewModelFactory.getFactory(context)
    )

    HomePageContent(
        viewModel = viewModel,
        onCreatePostClick = onCreatePostClick,
        onPostClick = onPostClick,
        onViewDraftsClick = onViewDraftsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomePageContent(
    viewModel: PostViewModel,
    onCreatePostClick: () -> Unit,
    onPostClick: (String) -> Unit,
    onViewDraftsClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

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
                state.isLoading && state.posts.isEmpty() && state.drafts.isEmpty() -> {
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
                state.posts.isEmpty() && state.drafts.isEmpty() -> {
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
                        if (state.drafts.isNotEmpty()) {
                            items(state.drafts) { draft ->
                                DraftPostCardGrid(
                                    post = draft,
                                    onDeleteClick = { viewModel.deleteDraft(draft.id) },
                                    onPublishClick = {
                                        // TODO: 实现发布草稿功能
                                    }
                                )
                            }
                        }

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
    var isFavorite by remember { mutableStateOf(false) }

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
                val displayImages = remember(post) {
                    if (post.imageUrls.isNotEmpty()) {
                        post.imageUrls
                    } else {
                        post.localImageUris
                    }
                }

                if (displayImages.isNotEmpty()) {
                    val imageUrl = displayImages.first()

                    val isValidUri = imageUrl.startsWith("http") ||
                            imageUrl.startsWith("content") ||
                            imageUrl.startsWith("file")

                    if (isValidUri) {
                        val painter = rememberAsyncImagePainter(
                            model = imageUrl
                        )

                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF7C4DFF)
                                    )
                                }
                            }
                            is AsyncImagePainter.State.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF87CEEB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BrokenImage,
                                        contentDescription = "Failed to load",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            else -> {
                                Image(
                                    painter = painter,
                                    contentDescription = "Post image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
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

                if (post.isLocalOnly || post.needsUpload) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = Color(0xFFFFF3CD),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Local",
                                tint = Color(0xFF856404),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Local",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF856404),
                                fontSize = 10.sp
                            )
                        }
                    }
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
                        onClick = {
                            isFavorite = !isFavorite
                            onFavoriteClick()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.outline,
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
fun DraftPostCardGrid(
    post: PostData,
    onDeleteClick: () -> Unit = {},
    onPublishClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onPublishClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // 显示本地图片
                if (post.localImageUris.isNotEmpty()) {
                    val imageUrl = post.localImageUris.first()

                    val painter = rememberAsyncImagePainter(
                        model = imageUrl
                    )

                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF7C4DFF)
                                )
                            }
                        }
                        is AsyncImagePainter.State.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFFE082)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoteAdd,
                                    contentDescription = "Draft",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        else -> {
                            Image(
                                painter = painter,
                                contentDescription = "Draft image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFE082)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Draft",
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = Color(0xFFFFC107),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "DRAFT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFF856404),
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete draft",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = post.title.ifEmpty { "Untitled Draft" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF856404)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to edit and publish",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF856404),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${post.localImageUris.size} images",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF856404),
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = onPublishClick,
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28A745),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Publish",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomePage(
                onCreatePostClick = {},
                onPostClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostCardGridPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .width(180.dp)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            PostCardGrid(
                post = PostData(
                    id = "1",
                    userId = "user123",
                    userName = "JohnDoe",
                    title = "Found iPhone 13 Pro",
                    description = "Found near the library",
                    postType = "FOUND",
                    phone = "",
                    date = "05/12/2024",
                    category = "Electronic",
                    imageUrls = emptyList(),
                    localImageUris = listOf("content://media/external/images/media/12345"),
                    questions = emptyList(),
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                onFavoriteClick = {},
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DraftPostCardGridPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier
                .width(180.dp)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            DraftPostCardGrid(
                post = PostData(
                    id = "draft_1",
                    title = "Lost Wallet Draft",
                    description = "Lost near campus",
                    postType = "LOST",
                    category = "Others",
                    localImageUris = listOf("content://media/external/images/media/12345"),
                    isDraft = true,
                    isLocalOnly = true,
                    timestamp = System.currentTimeMillis()
                ),
                onDeleteClick = {},
                onPublishClick = {}
            )
        }
    }
}