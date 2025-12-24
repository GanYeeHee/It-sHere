package com.example.itshere

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.itshere.data.ClaimRequest
import com.example.itshere.viewModel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaimedScreen(navController: NavController, viewModel: PostViewModel) {
    // 1. Correct: Listening to the shared ViewModel's state
    val approvedList by viewModel.approvedClaims.collectAsState()
    var selectedRequest by remember { mutableStateOf<ClaimRequest?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Claimed", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Background Image
            Image(
                painter = painterResource(R.drawable.take),
                contentDescription = null,
                modifier = Modifier.size(250.dp).align(Alignment.Center).alpha(0.1f)
            )

            // 2. Correct: Using the list from StateFlow
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(approvedList) { request ->
                    RequestSummaryCard(request = request, onClick = { selectedRequest = request })
                }
            }
        }
    }

    // 3. Detail Dialog - showActions is false so admin can't re-approve/reject
    selectedRequest?.let { request ->
        RequestDetailDialog(
            request = request,
            viewModel = viewModel,
            onDismiss = { selectedRequest = null },
            onAccept = {},
            onReject = {},
            showActions = false
        )
    }
}

