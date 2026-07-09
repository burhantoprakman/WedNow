package com.maritsa.app.domain.model

/**
 * Unified notification record stored in Firestore and shown in the in-app
 * Notification Center.
 *
 * Firestore path: weddings/{weddingId}/notifications/{notificationId}
 *
 * Broadcast vs. personal:
 *   recipientId = ""  → all wedding members receive this notification
 *   recipientId = id  → only that identity receives it (photo like, etc.)
 *
 * Push delivery:
 *   Cloud Functions fires on every new document and routes FCM messages
 *   according to the recipientId and type priority.
 */
data class AppNotification(
    val id: String = "",
    val weddingId: String = "",
    /** Empty string = broadcast to all members. Otherwise the recipient's identityId. */
    val recipientId: String = "",
    /** Identity ID of who triggered this notification. */
    val senderId: String = "",
    val senderName: String = "",
    val type: String = NotificationType.ANNOUNCEMENT,
    val title: String = "",
    val body: String = "",
    /** The entity this notification is about (photoId, postId, weddingId…). */
    val targetId: String = "",
    /** Destination screen name for deep linking — see [NotificationTargetScreen]. */
    val targetScreen: String = "",
    val createdAt: Long = 0L,
    /** Optional sender avatar or content thumbnail URL. */
    val imageUrl: String = "",
)

/**
 * All supported notification types, split by push-notification priority.
 *
 * HIGH PRIORITY  → push notification always sent
 * MEDIUM PRIORITY → push notification sent (can be silenced by user prefs later)
 * IN-APP ONLY    → never triggers a push; only visible in the notification center
 */
object NotificationType {

    // ── HIGH PRIORITY ─────────────────────────────────────────────────────────
    /** Admin/co-admin changed wedding date, venue, dress code, schedule, etc. */
    const val WEDDING_UPDATE = "wedding_update"

    /** Admin/co-admin posted a pinned announcement in the group chat. */
    const val ANNOUNCEMENT = "announcement"

    // ── MEDIUM PRIORITY ───────────────────────────────────────────────────────
    /** Someone liked the recipient's photo. */
    const val PHOTO_LIKE = "photo_like"

    /** Someone commented on the recipient's photo. */
    const val PHOTO_COMMENT = "photo_comment"

    /** Someone reacted to / liked the recipient's guestbook memory. */
    const val GUESTBOOK_LIKE = "guestbook_like"

    // ── IN-APP ONLY ───────────────────────────────────────────────────────────
    /** A new guest joined the wedding. */
    const val GUEST_JOINED = "guest_joined"

    /** A guest updated their RSVP status. */
    const val RSVP_UPDATE = "rsvp_update"
    const val SYSTEM = "system"

    /** Types that ALWAYS trigger a push notification (high priority). */
    val highPriority = setOf(WEDDING_UPDATE, ANNOUNCEMENT)

    /** Types that ALSO trigger a push notification (medium priority). */
    val mediumPriority = setOf(PHOTO_LIKE, PHOTO_COMMENT, GUESTBOOK_LIKE)

    /** Returns true if this type should trigger a push notification. */
    fun sendsPush(type: String) = type in highPriority || type in mediumPriority
}

/**
 * Screen name constants used in [AppNotification.targetScreen] so push-tap
 * deep links know where to navigate.
 */
object NotificationTargetScreen {
    const val PHOTOS = "photos"
    const val GUESTBOOK = "guestbook"
    const val CHAT = "chat"
    const val WEDDING_INFO = "wedding_info"
    const val HOME = "home"
    const val NOTIFICATIONS = "notifications"
}
