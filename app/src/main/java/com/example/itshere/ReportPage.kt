package com.example.itshere

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.itshere.viewModel.PostViewModel
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPage(navController: NavController, viewModel: PostViewModel) {
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("All") }
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsState()
    val approvedList by viewModel.requests.collectAsState() // for claimed items

    // Filter posts based on date
    val filteredPosts = remember(state.posts, selectedOption, customStartDate, customEndDate) {
        state.posts.filter { post ->
            if (selectedOption == "Custom" && customStartDate.isNotEmpty() && customEndDate.isNotEmpty()) {
                val postDate = post.timestampToDateString()
                postDate >= customStartDate && postDate <= customEndDate
            } else true
        }
    }

    val foundPosts = filteredPosts.filter { it.postType == "FOUND" }
    val lostPosts = filteredPosts.filter { it.postType == "LOST" }

    val approvedClaims = approvedList.filter { claim ->
        if (selectedOption == "Custom" && customStartDate.isNotEmpty() && customEndDate.isNotEmpty()) {
            val postDate = filteredPosts.find { it.id == claim.postId }?.timestampToDateString() ?: ""
            postDate >= customStartDate && postDate <= customEndDate
        } else true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                            .clickable { showDateDialog = true }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedOption == "All") "All Dates" else "$customStartDate → $customEndDate",
                                fontSize = 18.sp
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Title
                item {
                    Text(
                        text = "Total Report",
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black
                    )
                }

                // Found Item Section
                item {
                    ReportSection(
                        title = "Found Item",
                        titleColor = Color(0xFF7C4DFF),
                        posts = foundPosts
                    )
                }

                // Lost Item Section
                item {
                    ReportSection(
                        title = "Lost Item",
                        titleColor = Color(0xFFFF9800),
                        posts = lostPosts
                    )
                }

                // Claimed Item Section
                item {
                    ReportSection(
                        title = "Claimed Item",
                        titleColor = Color(0xFF4CAF50),
                        posts = approvedClaims.mapNotNull { claim ->
                            filteredPosts.find { it.id == claim.postId }
                        }
                    )
                }
            }

            // Download CSV Button
            Button(
                onClick = {
                    generatePdfReport(
                        context = navController.context,
                        foundPosts = foundPosts,
                        lostPosts = lostPosts,
                        claimedPosts = approvedClaims.mapNotNull { claim ->
                            filteredPosts.find { it.id == claim.postId }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text(text = "Download PDF")
            }
        }
    }

    // Date Filter Dialog
    if (showDateDialog) {
        DateFilterDialog(
            showDialog = true,
            selectedOption = selectedOption,
            onOptionChange = { selectedOption = it },
            startDate = customStartDate,
            endDate = customEndDate,
            onStartDateChange = { customStartDate = it },
            onEndDateChange = { customEndDate = it },
            onConfirm = { showDateDialog = false },
            onDismiss = { showDateDialog = false }
        )
    }
}

@Composable
fun ReportSection(title: String, titleColor: Color, posts: List<com.example.itshere.Data.PostData>) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Subtitle box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(titleColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                color = titleColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                val categoryCounts = posts.groupingBy { it.category }.eachCount()
                categoryCounts.forEach { (category, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = category)
                        Text(text = count.toString())
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total")
                    Text(posts.size.toString())
                }
            }
        }
    }
}

// CSV helpers
fun generatePdfReport(
    context: Context,
    foundPosts: List<com.example.itshere.Data.PostData>,
    lostPosts: List<com.example.itshere.Data.PostData>,
    claimedPosts: List<com.example.itshere.Data.PostData>,
    fileName: String = "report.pdf"
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply { textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }

        var y = 40f

        fun drawSection(title: String, posts: List<com.example.itshere.Data.PostData>) {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText(title, 40f, y, paint)
            y += 25f
            paint.textSize = 14f
            paint.isFakeBoldText = false

            val categoryCounts = posts.groupingBy { it.category }.eachCount()
            categoryCounts.forEach { (cat, count) ->
                canvas.drawText("$cat: $count", 60f, y, paint)
                y += 20f
            }
            canvas.drawText("Total: ${posts.size}", 60f, y, paint)
            y += 30f
        }

        drawSection("Found Item", foundPosts)
        drawSection("Lost Item", lostPosts)
        drawSection("Claimed Item", claimedPosts)

        pdfDocument.finishPage(page)

        // Save to Downloads
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { stream ->
                pdfDocument.writeTo(stream)
            }
            Toast.makeText(context, "PDF saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun saveCsvToDownloads(context: Context, csvData: String, fileName: String = "report.csv") {
    try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri).use { stream: OutputStream? ->
                stream?.write(csvData.toByteArray())
            }
            Toast.makeText(context, "Report saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to save report", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving report: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
