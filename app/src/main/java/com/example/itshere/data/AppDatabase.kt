package com.example.itshere.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.itshere.data.dao.AdminDao
import com.example.itshere.data.dao.LocalImageDao
import com.example.itshere.data.dao.NotificationDao
import com.example.itshere.data.dao.UserDao
import com.example.itshere.data.entity.Admin
import com.example.itshere.data.entity.LocalImage
import com.example.itshere.data.entity.Notification
import com.example.itshere.data.entity.User
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@Database(
    entities = [
        User::class,
        Admin::class,
        LocalImage::class,
        Notification::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun localImageDao(): LocalImageDao
    abstract fun adminDao(): AdminDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "itshere_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}