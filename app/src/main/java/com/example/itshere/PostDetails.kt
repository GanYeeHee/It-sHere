package com.example.itshere

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.itshere.Data.PostData
import com.example.itshere.ViewModel.PostViewModel
import com.example.itshere.ViewModel.PostViewModelFactory
import java.io.File

@Composable
fun PostDetailsScreen(
    postId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel(
        factory = PostViewModelFactory.getFactory(context)
    )

    val state by viewModel.state.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val post = state.posts.find { it.id == postId }

    val isFavorite = favorites.contains(postId)

    if (post == null) {
        // Loading or error state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF824DFF))
        }
        return
    }

    PostDetailsContent(
        post = post.copy(isFavorite = isFavorite),
        onBackClick = onBackClick,
        onShareClick = {
            sharePost(context, post)
        },
        onFavoriteClick = {
            viewModel.toggleFavorite(postId)
        }
    )
}

private fun sharePost(context: Context, post: PostData) {
    val shareText = buildString {
        append("Check out this post on It's Here!\n\n")
        append("Title: ${post.title}\n")
        append("Type: ${post.postType}\n")
        if (post.description.isNotBlank()) {
            append("Description: ${post.description}\n")
        }
        if (post.category.isNotBlank()) {
            append("Category: ${post.category}\n")
        }
        append("\nShared via It's Here App")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    try {
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                "Share post via"
            )
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun PostDetailsContent(
    post: PostData,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit
) {
    var showQuestionDialog by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            BottomActionSection(
                isFavorite = post.isFavorite,
                postType = post.postType,
                onActionClick = {
                    if (post.postType == "FOUND") {
                        showQuestionDialog = true
                    } else {
                        showContactDialog = true
                    }
                },
                onShareClick = onShareClick,
                onFavoriteClick = {
                    onFavoriteClick(!post.isFavorite)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Top Navigation (Back Button)
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Image Display Section
            Spacer(modifier = Modifier.height(24.dp))
            PostImageSection(imageUrls = post.imageUrls)

            // Tags (Type & Category)
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(
                    text = post.postType,
                    backgroundColor = if (post.postType == "FOUND") Color(0xFFE1BEE7) else Color(0xFFBBDEFB),
                    textColor = if (post.postType == "FOUND") Color(0xFF6A1B9A) else Color(0xFF1976D2)
                )

                if (post.category.isNotEmpty()) {
                    StatusChip(
                        text = post.category,
                        backgroundColor = Color(0xFFFFE0B2),
                        textColor = Color(0xFFE65100)
                    )
                }
            }

            // Title and Bookmark Button
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = post.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 34.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Info Section
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow(label = "Date", value = post.date)

            // Description Text
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.description.ifEmpty { "No description provided." },
                fontSize = 15.sp,
                color = Color(0xFF424242),
                lineHeight = 24.sp
            )

            // Questions Section (if FOUND post)
            if (post.postType == "FOUND" && post.questions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Verification Questions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Answer these questions correctly to claim this item",
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(16.dp))

                post.questions.forEachIndexed { index, qa ->
                    QuestionCard(
                        questionNumber = index + 1,
                        question = qa["question"] ?: ""
                    )
                    if (index < post.questions.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Extra bottom space
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // Question Dialog for FOUND items
    if (showQuestionDialog && post.questions.isNotEmpty()) {
        QuestionAnswerDialog(
            questions = post.questions,
            onDismiss = { showQuestionDialog = false },
            onSubmit = { answers ->
                showQuestionDialog = false
            }
        )
    }

    // Contact Dialog for LOST items
    if (showContactDialog) {
        ContactDialog(
            phone = post.phone,
            onDismiss = { showContactDialog = false }
        )
    }
}

@Composable
fun QuestionAnswerDialog(
    questions: List<Map<String, String>>,
    onDismiss: () -> Unit,
    onSubmit: (List<String>) -> Unit
) {
    var answers by remember { mutableStateOf(List(questions.size) { "" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Verify Ownership",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Please answer the following questions to claim this item:",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(16.dp))

                questions.forEachIndexed { index, qa ->
                    Text(
                        text = "${index + 1}. ${qa["question"]}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = answers[index],
                        onValueChange = { newAnswer ->
                            answers = answers.toMutableList().apply {
                                this[index] = newAnswer
                            }
                        },
                        placeholder = { Text("Your answer...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF824DFF),
                            cursorColor = Color(0xFF824DFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (index < questions.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(answers) },
                enabled = answers.all { it.isNotBlank() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF824DFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF666666))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ContactDialog(
    phone: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone",
                tint = Color(0xFF824DFF),
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Contact Information",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "You can contact the owner at:",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = phone,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF824DFF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF824DFF)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Got it")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PostImageSection(imageUrls: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrls.isNotEmpty()) {
            val imagePath = imageUrls.first()
            val imageFile = File(imagePath)

            if (imageFile.exists() && imageFile.canRead()) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageFile),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Image not found",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Image not available",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "No Image",
                    tint = Color(0xFF999999),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No image",
                    color = Color(0xFF999999),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.Black
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = label,
                tint = Color(0xFF824DFF),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun QuestionCard(questionNumber: Int, question: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF824DFF),
                modifier = Modifier.size(28.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = questionNumber.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = question,
                fontSize = 15.sp,
                color = Color(0xFF424242),
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BottomActionSection(
    isFavorite: Boolean,
    postType: String,
    onActionClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF824DFF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text(
                    text = if (postType == "FOUND") "Request for claim" else "Contact User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(56.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = { onFavoriteClick(!isFavorite) },
                modifier = Modifier.size(56.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isFavorite) Color.Red else Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Post Details - FOUND")
@Composable
fun PostDetailsFoundPreview() {
    val dummyPost = PostData(
        id = "preview_id",
        title = "Blue Backpack (Nike)",
        description = "I found a blue backpack near the cafeteria. It has a keychain of a bear on it. Please contact me if it's yours.\n\nFound it around 2:00 PM yesterday.",
        postType = "FOUND",
        category = "Bag",
        date = "10/12/2023",
        phone = "",
        isFavorite = false,
        imageUrls = emptyList(),
        questions = listOf(
            mapOf("question" to "What color is the keychain?", "answer" to "Brown bear"),
            mapOf("question" to "What's inside the main pocket?", "answer" to "Laptop"),
            mapOf("question" to "Where did you last see it?", "answer" to "Library")
        )
    )

    MaterialTheme {
        PostDetailsContent(
            post = dummyPost,
            onBackClick = {},
            onShareClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Post Details - LOST")
@Composable
fun PostDetailsLostPreview() {
    val dummyPost = PostData(
        id = "preview_id",
        title = "Lost AirPods Pro",
        description = "I lost my AirPods Pro case somewhere in the library. It has a red sticker on it. Please contact me if you found it!",
        postType = "LOST",
        category = "Electronic",
        date = "09/12/2023",
        phone = "60112345678",
        isFavorite = true,
        imageUrls = emptyList(),
        questions = emptyList()
    )

    MaterialTheme {
        PostDetailsContent(
            post = dummyPost,
            onBackClick = {},
            onShareClick = {},
            onFavoriteClick = {}
        )
    }
}