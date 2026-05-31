package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationType
import com.wednowapp.wednow.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

/**
 * Generic broadcast-notification use case used by [NotificationsViewModel].
 *
 * For event-driven notifications prefer the dedicated use cases:
 *   • [SendAnnouncementUseCase]
 *   • [SendWeddingUpdateNotificationUseCase]
 *   • [SendPhotoLikeNotificationUseCase]
 *   • [SendGuestbookLikeNotificationUseCase]
 */
class SendNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(
        weddingId: String,
        title: String,
        body: String,
        type: String = NotificationType.ANNOUNCEMENT,
    ): Result<Unit> {
        val senderId = identityManager.currentIdentityId
        val senderName = GuestSessionManager.getGuestName(context).ifBlank { "Admin" }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            weddingId = weddingId,
            recipientId = "",
            senderId = senderId,
            senderName = senderName,
            type = type,
            title = title.trim(),
            body = body.trim(),
            createdAt = System.currentTimeMillis(),
        )
        return notificationRepository.sendNotification(weddingId, notification)
    }
}
