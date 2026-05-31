package com.wednowapp.wednow.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wednowapp.wednow.MainActivity
import com.wednowapp.wednow.R
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.core.session.WeddingSessionManager
import com.wednowapp.wednow.data.remote.FcmTokenService
import com.wednowapp.wednow.domain.model.NotificationType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles Firebase Cloud Messaging events:
 *   • onNewToken   — saves fresh token to Firestore so Cloud Functions can deliver pushes
 *   • onMessageReceived — shows a local system notification when the app is in foreground
 *     (background delivery is handled automatically by FCM)
 */
@AndroidEntryPoint
class WedNowMessagingService : FirebaseMessagingService() {

    @Inject lateinit var fcmTokenService: FcmTokenService
    @Inject
    lateinit var identityManager: IdentityManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Notification channel IDs ──────────────────────────────────────────────

    companion object {
        const val CHANNEL_HIGH = "wednow_high"
        const val CHANNEL_MEDIUM = "wednow_medium"

        /** Notification data key constants (must match Cloud Functions). */
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_TYPE = "type"
        const val KEY_WEDDING_ID = "weddingId"
        const val KEY_TARGET_ID = "targetId"
        const val KEY_TARGET_SCREEN = "targetScreen"
    }

    // ── Token management ──────────────────────────────────────────────────────

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val weddingId = WeddingSessionManager.getWeddingId(applicationContext) ?: return
        val guestId = GuestSessionManager.getGuestId(applicationContext)
        val identityId = identityManager.currentIdentityId
        serviceScope.launch {
            fcmTokenService.saveToken(weddingId, guestId, identityId, token)
        }
    }

    // ── Message received (foreground) ─────────────────────────────────────────

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val title = data[KEY_TITLE] ?: remoteMessage.notification?.title ?: return
        val body = data[KEY_BODY] ?: remoteMessage.notification?.body ?: return
        val type = data[KEY_TYPE] ?: ""

        ensureNotificationChannels()
        showNotification(
            title = title,
            body = body,
            type = type,
            weddingId = data[KEY_WEDDING_ID].orEmpty(),
            targetScreen = data[KEY_TARGET_SCREEN].orEmpty(),
        )
    }

    // ── Show local notification ───────────────────────────────────────────────

    private fun showNotification(
        title: String,
        body: String,
        type: String,
        weddingId: String,
        targetScreen: String,
    ) {
        val channelId = if (type in NotificationType.highPriority) CHANNEL_HIGH else CHANNEL_MEDIUM

        // Tap opens MainActivity; pass weddingId + targetScreen so the app can deep-link
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(KEY_WEDDING_ID, weddingId)
            putExtra(KEY_TARGET_SCREEN, targetScreen)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (type in NotificationType.highPriority) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    // ── Channel creation ──────────────────────────────────────────────────────

    private fun ensureNotificationChannels() {
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

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
