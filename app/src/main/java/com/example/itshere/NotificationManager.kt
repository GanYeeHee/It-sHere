package com.example.itshere

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.itshere.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "new_post_channel"
        const val CHANNEL_NAME = "New Posts"
        const val CHANNEL_DESCRIPTION = "Notifications for new posts"
        const val NOTIFICATION_GROUP = "post_notifications_group"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as AndroidNotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    suspend fun showNewPostNotification(
        postId: String,
        postTitle: String,
        postType: String,
        category: String
    ) {
        withContext(Dispatchers.Main) {
            val notificationId = postId.hashCode() and 0xfffffff

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New ${postType} Post!")
                .setContentText(postTitle)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("New $postType: $postTitle\n\nCategory: $category\n\nTap to view details")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setGroup(NOTIFICATION_GROUP)
                .setGroupSummary(true)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setLights(Color.BLUE, 1000, 1000)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)

            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(notificationId, builder.build())
                    println("Notification shown for post: $postTitle")
                } else {
                    println("Notifications not enabled")
                }
            }
        }
    }

    fun clearAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}