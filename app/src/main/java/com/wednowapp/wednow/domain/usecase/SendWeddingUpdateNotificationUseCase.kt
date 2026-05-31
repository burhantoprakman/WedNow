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
 * Broadcasts a wedding-update notification to all members when an admin or
 * co-admin saves changes to the wedding details (date, venue, dress code, etc.).
 *
 * This is a HIGH PRIORITY notification — Cloud Functions will deliver it as a
 * push notification to every guest's device.
 */
class SendWeddingUpdateNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) {

    /**
     * @param weddingId   The wedding that was updated.
     * @param changeDescription  Short human-readable description of what changed,
     *                           e.g. "Wedding venue has been updated".
     */
    suspend operator fun invoke(
        weddingId: String,
        changeDescription: String = "Wedding details have been updated.",
    ): Result<Unit> {
        val senderId = identityManager.currentIdentityId
        val senderName = GuestSessionManager.getGuestName(context).ifBlank { "The wedding team" }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            weddingId = weddingId,
            recipientId = "",          // broadcast — all members receive this
            senderId = senderId,
            senderName = senderName,
            type = NotificationType.WEDDING_UPDATE,
            title = "Wedding update 💍",
            body = changeDescription,
            targetId = weddingId,
            targetScreen = NotificationTargetScreen.WEDDING_INFO,
            createdAt = System.currentTimeMillis(),
        )
        return notificationRepository.sendNotification(weddingId, notification)
    }
}
