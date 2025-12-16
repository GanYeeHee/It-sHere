package com.example.itshere.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.itshere.Data.PostData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class ReportData(
    val total: Int = 0,
    val lost: Int = 0,
    val found: Int = 0,
    val claimed: Int = 0,
    val byCategory: Map<String, Map<String, Int>> = emptyMap()
)

class ReportViewModel(private val context: Context) : ViewModel() {

    private val TAG = "ReportViewModel"
    private val firestore = FirebaseFirestore.getInstance()

    private val _reportData = MutableStateFlow(ReportData())
    val reportData: StateFlow<ReportData> = _reportData

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Load report data from Firebase and optionally filter by date range.
     * @param startDate inclusive start date (yyyy-MM-dd)
     * @param endDate inclusive end date (yyyy-MM-dd)
     */
    fun loadReportData(startDate: String? = null, endDate: String? = null) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val posts = snapshot.documents.mapNotNull { it.toObject(PostData::class.java) }

                // Filter by date if startDate and endDate are provided
                val filteredPosts = if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
                    val startMillis = dateFormat.parse(startDate)?.time ?: 0L
                    val endMillis = (dateFormat.parse(endDate)?.time ?: Long.MAX_VALUE) + 24 * 60 * 60 * 1000 - 1 // include full day
                    posts.filter { post ->
                        val postMillis = post.timestamp ?: 0L
                        postMillis in startMillis..endMillis
                    }
                } else posts

                val lost = filteredPosts.count { it.postType == "LOST" }
                val found = filteredPosts.count { it.postType == "FOUND" }
                val claimed = filteredPosts.count { it.isClaimed } // assumes PostData has isClaimed boolean
                val total = filteredPosts.size

                val byCategory = filteredPosts.groupBy { it.category }.mapValues { entry ->
                    mapOf(
                        "lost" to entry.value.count { it.postType == "LOST" },
                        "found" to entry.value.count { it.postType == "FOUND" },
                        "claimed" to entry.value.count { it.isClaimed }
                    )
                }

                _reportData.value = ReportData(
                    total = total,
                    lost = lost,
                    found = found,
                    claimed = claimed,
                    byCategory = byCategory
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error loading report data: ${e.message}")
            }
        }
    }
}
