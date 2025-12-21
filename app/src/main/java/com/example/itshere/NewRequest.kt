package com.example.itshere

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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


@Composable
fun NewRequestScreen(
    navController: NavController,
    viewModel: PostViewModel,
    modifier: Modifier = Modifier
) {

    val requests by viewModel.requests.collectAsState()

    Scaffold(
        topBar = {
            Box(modifier = modifier.fillMaxWidth().padding(top = 40.dp)) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "New Requests",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Background Image
            Image(
                painter = painterResource(R.drawable.bell),
                contentDescription = null,
                modifier = Modifier.size(250.dp).align(Alignment.Center).alpha(0.05f)
            )

            if (requests.isEmpty()) {
                Text("No requests yet", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = requests) { claim ->
                        RequestCard(request = claim)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestCard(request: ClaimRequest) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.postTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                // ADD THE TIME AGO HERE
                Text(
                    text = getRelativeTime(request.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = "New Claim Request",
                color = Color(0xFF824DFF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // Display the 3 answers
            request.answers.forEachIndexed { index, answer ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "Q${index + 1}: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF824DFF)
                    )
                    Text(
                        text = answer,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
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
