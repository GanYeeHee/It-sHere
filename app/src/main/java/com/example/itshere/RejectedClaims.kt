package com.example.itshere

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.itshere.Data.ClaimRequest
import com.example.itshere.viewModel.PostViewModel
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RejectedScreen(navController: NavController, viewModel: PostViewModel) {
    var rejectedList by remember { mutableStateOf(listOf<ClaimRequest>()) }
    var selectedRequest by remember { mutableStateOf<ClaimRequest?>(null) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("rejected_claims")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ClaimRequest::class.java)?.copy(id = doc.id)
                    }
                    rejectedList = items
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Rejected", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    // Using your custom back_arrow image
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
                painter = painterResource(R.drawable.take),
                contentDescription = null,
                modifier = Modifier.size(250.dp).align(Alignment.Center).alpha(0.1f)
            )

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

    // Show Full Details
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

