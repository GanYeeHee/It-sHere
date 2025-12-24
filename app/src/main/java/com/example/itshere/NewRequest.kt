package com.example.itshere

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.itshere.data.ClaimRequest
import com.example.itshere.viewModel.PostViewModel
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    navController: NavController,
    viewModel: PostViewModel,
    modifier: Modifier = Modifier
) {

    val requests by viewModel.requests.collectAsState()
    var selectedRequest by remember { mutableStateOf<ClaimRequest?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Requests", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(modifier = Modifier.weight(1f)) {
                // Background Image
                Image(
                    painter = painterResource(R.drawable.bell),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center)
                        .alpha(0.05f)
                )

                if (requests.isEmpty()) {
                    // THIS is what the admin will see the moment the button is pressed
                    Text(
                        text = "No request yet",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests) { claim ->
                            RequestSummaryCard(request = claim, onClick = { selectedRequest = claim })
                        }
                    }
                }
            }
        }

        selectedRequest?.let { request ->
            RequestDetailDialog(
                request = request,
                viewModel = viewModel,
                onDismiss = { selectedRequest = null },
                onAccept = {
                    viewModel.processClaim(request, "approve") // Moves to 'approved_claims'
                    selectedRequest = null
                },
                onReject = {
                    viewModel.processClaim(request, "reject") // Moves to 'rejected_claims'
                    selectedRequest = null
                },
                showActions = true
            )
        }
    }
}

@Composable
fun RequestSummaryCard(request: ClaimRequest, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = request.postTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "New claim request", color = Color(0xFF824DFF), fontSize = 14.sp)
            }
            Text(
                text = getRelativeTime(request.timestamp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RequestDetailDialog(
    request: ClaimRequest,
    viewModel: PostViewModel,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    showActions: Boolean = true // Added: Defaults to true for the New Request Screen
) {
    val originalPost = viewModel.getPostById(request.postId)

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Back Icon and Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "Item id : ${request.postId.take(8)}",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "from : ${request.requesterId}",
                            fontSize = 16.sp,
                            color = Color(0xFF824DFF),
                            fontWeight = FontWeight.Bold
                        )

                        val formatter = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                        val exactDate = formatter.format(java.util.Date(request.timestamp))
                        Text(
                            text = "date: $exactDate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = request.postTitle,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 3. Original Post Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    val imageUrl = originalPost?.imageUrls?.firstOrNull()
                    if (imageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(File(imageUrl)),
                            contentDescription = "Item Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp), tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Verification Part
                Text(text = "Verification:", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(12.dp))

                originalPost?.questions?.forEachIndexed { index, qaMap ->
                    val question = qaMap["question"] ?: ""
                    val correctAnswer = qaMap["answer"] ?: ""
                    val requesterAnswer = request.answers.getOrNull(index) ?: ""

                    Surface(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "${index + 1}. $question", fontWeight = FontWeight.Bold, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Correct Answer:", fontSize = 12.sp, color = Color.Gray)
                            Text(text = correctAnswer, color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Requester's Answer:", fontSize = 12.sp, color = Color.Gray)
                            Text(text = requesterAnswer, color = if(correctAnswer.equals(requesterAnswer, true)) Color.Black else Color.Red, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // MODIFIED SECTION: Only show buttons if showActions is true
                if (showActions) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) { Text("Reject", color = Color.White, fontWeight = FontWeight.Bold) }

                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) { Text("Accept", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                } else {
                    // Optional: Add a small padding at the bottom so the last question isn't touching the edge
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    // This built-in Android function handles "1 min ago", "2 hours ago", etc.
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        now,
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
