package com.wednowapp.wednow

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WedNowApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (manager.getNotificationChannel(CHANNEL_HIGH) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_HIGH,
                    "Wedding Updates & Announcements",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Important wedding updates and admin announcements."
                    enableVibration(true)
                }
            )
        }

        if (manager.getNotificationChannel(CHANNEL_MEDIUM) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MEDIUM,
                    "Photo Likes & Reactions",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Photo likes, comments, and guestbook reactions."
                }
            )
        }
    }

    companion object {
        const val CHANNEL_HIGH = "wednow_high"
        const val CHANNEL_MEDIUM = "wednow_medium"
    }
}
