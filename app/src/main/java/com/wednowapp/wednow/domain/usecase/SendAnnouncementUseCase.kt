package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.identity.IdentityManager
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationTargetScreen
import com.wednowapp.wednow.domain.model.NotificationType
import com.wednowapp.wednow.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject

/**
 * Broadcasts an admin announcement to all wedding members.
 *
 * This is a HIGH PRIORITY notification — every guest will receive a push
 * notification (except the sender themselves).
 *
 * Only admins and co-admins should be allowed to call this; the caller
 * (ViewModel) is responsible for enforcing that gate via [PermissionService].
 */
class SendAnnouncementUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) {

    /**
     * @param weddingId  The wedding this announcement belongs to.
     * @param title      Short announcement heading (e.g. "Ceremony starts in 1 hour").
     * @param body       Full announcement text.
     */
    suspend operator fun invoke(
        weddingId: String,
        title: String,
        body: String,
    ): Result<Unit> {
        val senderId = identityManager.currentIdentityId
        val senderName = GuestSessionManager.getGuestName(context).ifBlank { "Admin" }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            weddingId = weddingId,
            recipientId = "",          // broadcast
            senderId = senderId,
            senderName = senderName,
            type = NotificationType.ANNOUNCEMENT,
            title = "📢 $title",
            body = body,
            targetId = weddingId,
            targetScreen = NotificationTargetScreen.NOTIFICATIONS,
            createdAt = System.currentTimeMillis(),
        )
        return notificationRepository.sendNotification(weddingId, notification)
    }
}
