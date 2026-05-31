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
 * Sends a MEDIUM PRIORITY notification to the photo owner when someone likes
 * their photo.
 *
 * Rules enforced here:
 *   • No notification when the liker IS the photo owner (self-like).
 *   • No notification on unlike (callers must only invoke on a new like).
 *   • Deduplication is handled server-side by Cloud Functions (checks if a
 *     like-notification for this (senderId, photoId) pair already exists within
 *     the last 24 h).
 */
class SendPhotoLikeNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) {

    /**
     * @param weddingId        The wedding the photo belongs to.
     * @param photoId          The photo that was liked ([AppNotification.targetId]).
     * @param photoOwnerIdentityId  The identity ID of the person who uploaded the photo.
     */
    suspend operator fun invoke(
        weddingId: String,
        photoId: String,
        photoOwnerIdentityId: String,
    ): Result<Unit> {
        val likerId = identityManager.currentIdentityId

        // Never notify the owner when they like their own photo
        if (likerId == photoOwnerIdentityId || photoOwnerIdentityId.isBlank()) {
            return Result.success(Unit)
        }

        val likerName = GuestSessionManager.getGuestName(context).ifBlank { "Someone" }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            weddingId = weddingId,
            recipientId = photoOwnerIdentityId,
            senderId = likerId,
            senderName = likerName,
            type = NotificationType.PHOTO_LIKE,
            title = "New like ❤️",
            body = "$likerName liked your photo.",
            targetId = photoId,
            targetScreen = NotificationTargetScreen.PHOTOS,
            createdAt = System.currentTimeMillis(),
        )
        return notificationRepository.sendNotification(weddingId, notification)
    }
}
