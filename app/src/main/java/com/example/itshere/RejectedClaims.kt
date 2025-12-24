package com.example.itshere

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
fun RejectedScreen(navController: NavController, viewModel: PostViewModel) {
    // Correct: Collect live state from the shared ViewModel
    val rejectedList by viewModel.rejectedClaims.collectAsState()
    var selectedRequest by remember { mutableStateOf<ClaimRequest?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Rejected Claims", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
            // Background Image
            Image(
                painter = painterResource(R.drawable.stop),
                contentDescription = null,
                modifier = Modifier.size(250.dp).align(Alignment.Center).alpha(0.1f)
            )

            if (rejectedList.isEmpty()) {
                Text(
                    "No rejected claims",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = rejectedList) { requestItem ->
                        RequestSummaryCard(
                            request = requestItem,
                            onClick = { selectedRequest = requestItem }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog - showActions = false prevents the admin from trying to re-process it
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

