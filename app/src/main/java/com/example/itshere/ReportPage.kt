package com.example.itshere

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.itshere.viewModel.ReportData
import com.example.itshere.viewModel.ReportViewModel
import java.util.*

@Composable
fun ReportPage(
    onBack: () -> Unit,
    reportViewModel: ReportViewModel = viewModel(factory = androidx.lifecycle.ViewModelProvider.NewInstanceFactory())
) {
    val context = LocalContext.current

    // Load data when page starts
    LaunchedEffect(Unit) { reportViewModel.loadReportData() }

    val reportData by reportViewModel.reportData.collectAsState()

    // --- Date Filter State ---
    var selectedDateOption by remember { mutableStateOf("Overall") }
    var tempStartDate by remember { mutableStateOf("") }
    var tempEndDate by remember { mutableStateOf("") }
    var appliedStartDate by remember { mutableStateOf("") }
    var appliedEndDate by remember { mutableStateOf("") }
    var showDateDialog by remember { mutableStateOf(false) }

    // --- Report Type ---
    var selectedReportType by remember { mutableStateOf("Total Report") }

    Column(modifier = Modifier.fillMaxSize().padding(top = 60.dp)) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Report",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // Show date range as clickable box
            Box(
                modifier = Modifier
                    .background(Color(0xFFBFC6FF), RoundedCornerShape(8.dp))
                    .clickable { showDateDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (appliedStartDate.isNotEmpty() && appliedEndDate.isNotEmpty()) {
                        "$appliedStartDate - $appliedEndDate"
                    } else {
                        "Overall"
                    },
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Report Type Selector ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            val types = listOf("Total Report", "Lost Report", "Found Report", "Claimed Report", "Category Report")
            types.forEach { type ->
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            if (selectedReportType == type) Color(0xFFBFC6FF) else Color(0xFFE0E0E0),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedReportType = type }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(type, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Report Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedReportType) {
                "Total Report" -> {
                    ReportItem("Total Items", reportData.total)
                    ReportItem("Lost Items", reportData.lost)
                    ReportItem("Found Items", reportData.found)
                    ReportItem("Claimed Items", reportData.claimed)
                }
                "Lost Report" -> {
                    ReportItem("Lost Items", reportData.lost)
                }
                "Found Report" -> {
                    ReportItem("Found Items", reportData.found)
                }
                "Claimed Report" -> {
                    ReportItem("Claimed Items", reportData.claimed)
                }
                "Category Report" -> {
                    reportData.byCategory.forEach { (category, counts) ->
                        ReportItem("Category: $category", counts["lost"]!! + counts["found"]!! + counts["claimed"]!! )
                        ReportItem("Lost", counts["lost"] ?: 0, indent = 16.dp)
                        ReportItem("Found", counts["found"] ?: 0, indent = 16.dp)
                        ReportItem("Claimed", counts["claimed"] ?: 0, indent = 16.dp)
                    }
                }
            }
        }
    }

    // --- Date Selection Dialog ---
    if (showDateDialog) {
        AlertDialog(
            onDismissRequest = { showDateDialog = false },
            title = { Text("Select Date Range") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedDateOption == "Overall",
                            onClick = { selectedDateOption = "Overall" }
                        )
                        Text("Overall")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedDateOption == "Custom Range",
                            onClick = { selectedDateOption = "Custom Range" }
                        )
                        Text("Custom Range")
                    }

                    if (selectedDateOption == "Custom Range") {
                        OutlinedTextField(
                            value = tempStartDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Date") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val today = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d -> tempStartDate = "%04d-%02d-%02d".format(y, m + 1, d) },
                                        today.get(Calendar.YEAR),
                                        today.get(Calendar.MONTH),
                                        today.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }) { Text("📅") }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = tempEndDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("End Date") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val today = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d -> tempEndDate = "%04d-%02d-%02d".format(y, m + 1, d) },
                                        today.get(Calendar.YEAR),
                                        today.get(Calendar.MONTH),
                                        today.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }) { Text("📅") }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedDateOption == "Overall") {
                            appliedStartDate = ""
                            appliedEndDate = ""
                            reportViewModel.loadReportData() // no filter
                        } else {
                            appliedStartDate = tempStartDate
                            appliedEndDate = tempEndDate
                            reportViewModel.loadReportData(appliedStartDate, appliedEndDate)
                        }
                        showDateDialog = false
                    },
                    enabled = selectedDateOption == "Overall" || (tempStartDate.isNotEmpty() && tempEndDate.isNotEmpty())
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ReportItem(title: String, count: Int, indent: Dp = 0.dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontWeight = FontWeight.Medium)
        Text(count.toString(), fontWeight = FontWeight.Bold)
    }
}
