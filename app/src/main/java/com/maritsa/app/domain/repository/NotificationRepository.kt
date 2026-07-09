package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    /**
     * Real-time stream of all notifications relevant to [recipientId]:
     *   • broadcast notifications (recipientId = "")
     *   • personal notifications addressed to [recipientId]
     *
     * Ordered by [AppNotification.createdAt] descending (newest first).
     */
    fun observeNotifications(
        weddingId: String,
        recipientId: String,
    ): Flow<List<AppNotification>>

    /**
     * Real-time count of unread notifications for [recipientId].
     * "Unread" = notification not in the local read-set tracked by
     * [NotificationReadManager]; synced to Firestore on [markAsRead].
     */
    fun observeUnreadCount(
        weddingId: String,
        recipientId: String,
    ): Flow<Int>

    /** Persist a new notification. Cloud Functions will pick this up for push delivery. */
    suspend fun sendNotification(
        weddingId: String,
        notification: AppNotification,
    ): Result<Unit>

    /** Mark a single notification as read in Firestore (cross-device sync). */
    suspend fun markAsRead(
        weddingId: String,
        notificationId: String,
        recipientId: String,
    ): Result<Unit>

    /** Bulk mark-all-read convenience. */
    suspend fun markAllAsRead(
        weddingId: String,
        notificationIds: List<String>,
        recipientId: String,
    ): Result<Unit>
}
