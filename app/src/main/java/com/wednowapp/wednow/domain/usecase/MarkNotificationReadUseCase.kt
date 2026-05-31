package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * Marks a single notification as read in Firestore for cross-device sync.
 * The local read state is managed separately by [NotificationReadManager].
 */
class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(
        weddingId: String,
        notificationId: String,
        recipientId: String,
    ): Result<Unit> = repository.markAsRead(weddingId, notificationId, recipientId)
}

/**
 * Marks all supplied notifications as read in Firestore.
 */
class MarkAllNotificationsReadUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(
        weddingId: String,
        notificationIds: List<String>,
        recipientId: String,
    ): Result<Unit> = repository.markAllAsRead(weddingId, notificationIds, recipientId)
}
