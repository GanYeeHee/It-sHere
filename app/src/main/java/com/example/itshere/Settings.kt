package com.example.itshere

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import android.Manifest
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    onBackClick: () -> Unit = { navController?.popBackStack() }
) {
    val context = LocalContext.current
    val sharedPreferences = remember { PreferenceManager.getDefaultSharedPreferences(context) }

    // Load saved values from SharedPreferences
    var isLightTheme by remember {
        mutableStateOf(sharedPreferences.getBoolean("theme_light", true))
    }
    var notificationsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("notifications_enabled", false))
    }

    // Android 13+ notification permission request
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
            notificationsEnabled = true
            sharedPreferences.edit().putBoolean("notifications_enabled", true).apply()
            showTestNotification(
                context = context,
                title = "🔔 Notifications Enabled",
                message = "You will now receive notifications for your items"
            )
        } else {
            // Permission denied, keep it off
            notificationsEnabled = false
            sharedPreferences.edit().putBoolean("notifications_enabled", false).apply()
        }
    }

    // Initialize notification channel
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Settings
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    // Section Header
                    Text(
                        text = "APPEARANCE",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    // Theme Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Theme",
                                tint = Color(0xFF824DFF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Light theme",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Switch to dark mode",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                        Switch(
                            checked = isLightTheme,
                            onCheckedChange = { newValue ->
                                isLightTheme = newValue
                                // Save to SharedPreferences
                                sharedPreferences.edit()
                                    .putBoolean("theme_light", newValue)
                                    .apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF824DFF),
                                uncheckedThumbColor = Color(0xFFE0E0E0),
                                uncheckedTrackColor = Color(0xFFB0B0B0)
                            )
                        )
                    }

                    Divider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Settings
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    // Section Header
                    Text(
                        text = "NOTIFICATIONS",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    // All Notifications
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF824DFF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Notifications",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Enable all notifications",
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { newValue ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    // Android 13+ requires permission request
                                    if (newValue) {
                                        // Check if permission already granted
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            notificationsEnabled = true
                                            sharedPreferences.edit()
                                                .putBoolean("notifications_enabled", true)
                                                .apply()
                                            showTestNotification(
                                                context = context,
                                                title = "🔔 Notifications Enabled",
                                                message = "You will now receive notifications for your items"
                                            )
                                        } else {
                                            // Request permission
                                            requestPermissionLauncher.launch(
                                                Manifest.permission.POST_NOTIFICATIONS
                                            )
                                        }
                                    } else {
                                        notificationsEnabled = false
                                        sharedPreferences.edit()
                                            .putBoolean("notifications_enabled", false)
                                            .apply()
                                        showTestNotification(
                                            context = context,
                                            title = "🔕 Notifications Disabled",
                                            message = "You won't receive any notifications"
                                        )
                                    }
                                } else {
                                    // Android 12 and below
                                    notificationsEnabled = newValue
                                    sharedPreferences.edit()
                                        .putBoolean("notifications_enabled", newValue)
                                        .apply()

                                    showTestNotification(
                                        context = context,
                                        title = if (newValue) "🔔 Notifications Enabled" else "🔕 Notifications Disabled",
                                        message = if (newValue)
                                            "You will now receive notifications for your items"
                                        else
                                            "You won't receive any notifications"
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF824DFF),
                                uncheckedThumbColor = Color(0xFFE0E0E0),
                                uncheckedTrackColor = Color(0xFFB0B0B0)
                            )
                        )
                    }

                    Divider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Info
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    // Section Header
                    Text(
                        text = "APP INFO",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 20.dp)
                    )

                    // Version
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Version",
                                tint = Color(0xFF824DFF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Version",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }
                        Text(
                            text = "1.0.0",
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    }

                    Divider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Create notification channel
private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "app_notifications"
        val channelName = "App Notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = "Item-related notifications"
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 100, 200)
        channel.enableLights(true)
        channel.lightColor = android.graphics.Color.MAGENTA

        val notificationManager = context.getSystemService(NotificationManager::class.java)

        // Delete old channels if they exist
        notificationManager.deleteNotificationChannel("settings_notifications")
        notificationManager.deleteNotificationChannel("test_notifications")

        // Create new channel
        notificationManager.createNotificationChannel(channel)
    }
}

// Show test notification
private fun showTestNotification(context: Context, title: String, message: String) {
    try {
        val channelId = "app_notifications"
        val notificationId = System.currentTimeMillis().toInt()

        // Create notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(getNotificationIcon(context))
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Includes sound, vibration
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Show notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure channel exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                createNotificationChannel(context)
            }
        }

        notificationManager.notify(notificationId, notificationBuilder.build())

        // Log output
        println("✅ Notification sent: $title")
        println("📱 Channel ID: $channelId")

    } catch (e: Exception) {
        println("❌ Error sending notification: ${e.message}")
        e.printStackTrace()
    }
}

// Get notification icon
private fun getNotificationIcon(context: Context): Int {
    return try {
        // Try to use app's own icon
        context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
    } catch (e: Exception) {
        // If not found, use system icon
        android.R.drawable.ic_dialog_info
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}