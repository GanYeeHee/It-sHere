package com.example.itshere

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.itshere.viewModel.PostViewModel
import java.util.*
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream
import com.example.itshere.data.PostData
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportPage(navController: NavController, viewModel: PostViewModel) {
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("All") }
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }
    var showReportTypeMenu by remember { mutableStateOf(false) }
    var selectedReportType by remember { mutableStateOf("Total Report") }

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
                    // Only show the Date box here (optional)
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
                    // Report Type Dropdown anchored properly
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart) // Ensures dropdown appears below the box
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                                .clickable { showReportTypeMenu = true }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedReportType,
                                    fontSize = 18.sp
                                )
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = showReportTypeMenu,
                            onDismissRequest = { showReportTypeMenu = false },
                            modifier = Modifier.fillMaxWidth() // make it match parent width
                        ) {
                            listOf("Total Report", "Found Report", "Lost Report", "Category Report").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedReportType = type
                                        showReportTypeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }



                // Depending on report type
                if (selectedReportType == "Total Report" || selectedReportType == "Found Report") {
                    item {
                        ReportSection(
                            title = "Found Item",
                            titleColor = Color(0xFF7C4DFF),
                            posts = foundPosts,
                            showSubtitleBox = selectedReportType == "Total Report"
                        )
                    }
                }

                if (selectedReportType == "Total Report" || selectedReportType == "Lost Report") {
                    item {
                        ReportSection(
                            title = "Lost Item",
                            titleColor = Color(0xFFFF9800),
                            posts = lostPosts,
                            showSubtitleBox = selectedReportType == "Total Report"
                        )
                    }
                }

                if (selectedReportType == "Total Report") {
                    item {
                        ReportSection(
                            title = "Claimed Item",
                            titleColor = Color(0xFF4CAF50),
                            posts = approvedClaims.mapNotNull { claim ->
                                filteredPosts.find { it.id == claim.postId }
                            },
                            showSubtitleBox = true
                        )
                    }
                }

                if (selectedReportType == "Category Report") {
                    val categories = filteredPosts.map { it.category }.distinct()
                    categories.forEach { category ->
                        item {
                            CategoryReportSection(
                                category = category,
                                foundPosts = filteredPosts.filter { it.category == category && it.postType == "FOUND" },
                                lostPosts = filteredPosts.filter { it.category == category && it.postType == "LOST" }
                            )
                        }
                    }
                }
            }

            // Download PDF Button
            Button(
                onClick = {
                    savePdfReport(
                        navController.context,
                        selectedReportType,
                        foundPosts,
                        lostPosts,
                        approvedClaims.mapNotNull { claim -> filteredPosts.find { it.id == claim.postId } },
                        filteredPosts
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
fun ReportSection(
    title: String,
    titleColor: Color,
    posts: List<PostData>,
    showSubtitleBox: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        if (showSubtitleBox) {
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
        }

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

                if (posts.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
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
}

@Composable
fun CategoryReportSection(category: String, foundPosts: List<PostData>, lostPosts: List<PostData>) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // Category subtitle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF90CAF9).copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category, fontSize = 20.sp, color = Color(0xFF1976D2))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Found")
                    Text(foundPosts.size.toString())
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lost")
                    Text(lostPosts.size.toString())
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total")
                    Text((foundPosts.size + lostPosts.size).toString())
                }
            }
        }
    }
}

// PDF helpers
fun generatePdfReport(
    reportType: String,
    foundPosts: List<PostData>,
    lostPosts: List<PostData>,
    claimedPosts: List<PostData>,
    allPosts: List<PostData>
): String {
    // For simplicity, return CSV-style string (can be replaced with actual PDF generation)
    val builder = StringBuilder()
    builder.append("$reportType\n\n")

    fun appendSection(title: String, posts: List<PostData>) {
        builder.append("$title\n")
        val categoryCounts = posts.groupingBy { it.category }.eachCount()
        categoryCounts.forEach { (cat, count) ->
            builder.append("$cat - $count\n")
        }
        builder.append("Total - ${posts.size}\n\n")
    }

    when (reportType) {
        "Total Report" -> {
            appendSection("Found Item", foundPosts)
            appendSection("Lost Item", lostPosts)
            appendSection("Claimed Item", claimedPosts)
        }
        "Found Report" -> appendSection("Found Item", foundPosts)
        "Lost Report" -> appendSection("Lost Item", lostPosts)
        "Category Report" -> {
            val categories = allPosts.map { it.category }.distinct()
            categories.forEach { category ->
                val fPosts = allPosts.filter { it.category == category && it.postType == "FOUND" }
                val lPosts = allPosts.filter { it.category == category && it.postType == "LOST" }
                builder.append("$category\n")
                builder.append("Found - ${fPosts.size}\n")
                builder.append("Lost - ${lPosts.size}\n")
                builder.append("Total - ${fPosts.size + lPosts.size}\n\n")
            }
        }
    }

    return builder.toString()
}

fun savePdfToDownloads(context: Context, pdfData: String, fileName: String = "report.pdf") {
    try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri).use { stream: OutputStream? ->
                stream?.write(pdfData.toByteArray())
            }
            Toast.makeText(context, "PDF saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun savePdfReport(
    context: Context,
    reportType: String,
    foundPosts: List<PostData>,
    lostPosts: List<PostData>,
    claimedPosts: List<PostData>,
    allPosts: List<PostData>,
    selectedOption: String = "All",
    customStartDate: String = "",
    customEndDate: String = ""
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            textSize = 14f
            color = android.graphics.Color.BLACK
        }

        var y = 40f
        val lineHeight = 20f

        // Title
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText(reportType, 40f, y, paint)
        paint.textSize = 14f
        paint.isFakeBoldText = false
        y += lineHeight

        // Date/Time info
        val dateText = if (selectedOption == "All") {
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            "Report until $currentDate"
        } else {
            "Report from $customStartDate → $customEndDate"
        }
        canvas.drawText(dateText, 40f, y, paint)
        y += lineHeight * 2

        fun drawSection(title: String, posts: List<PostData>) {
            paint.isFakeBoldText = true
            canvas.drawText(title, 40f, y, paint)
            paint.isFakeBoldText = false
            y += lineHeight

            val categoryCounts = posts.groupingBy { it.category }.eachCount()
            categoryCounts.forEach { (category, count) ->
                canvas.drawText(category, 60f, y, paint)
                canvas.drawText(count.toString(), 300f, y, paint)
                y += lineHeight
            }

            canvas.drawText("Total", 60f, y, paint)
            canvas.drawText(posts.size.toString(), 300f, y, paint)
            y += lineHeight * 2
        }

        when (reportType) {
            "Total Report" -> {
                drawSection("Found Items", foundPosts)
                drawSection("Lost Items", lostPosts)
                drawSection("Claimed Items", claimedPosts)
            }
            "Found Report" -> drawSection("Found Items", foundPosts)
            "Lost Report" -> drawSection("Lost Items", lostPosts)
            "Category Report" -> {
                val categories = allPosts.map { it.category }.distinct()
                categories.forEach { category ->
                    drawSection(
                        category,
                        allPosts.filter { it.category == category }
                    )
                }
            }
        }

        pdfDocument.finishPage(page)

        // Save PDF to Downloads
        val resolver = context.contentResolver
        val fileName = "report_${System.currentTimeMillis()}.pdf"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri).use { stream: OutputStream? ->
                pdfDocument.writeTo(stream)
            }
            Toast.makeText(context, "PDF saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_LONG).show()
        }

        pdfDocument.close()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
