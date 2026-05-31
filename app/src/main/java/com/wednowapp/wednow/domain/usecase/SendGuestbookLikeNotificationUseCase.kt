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
 * Sends a MEDIUM PRIORITY notification to the guestbook memory owner when
 * someone reacts to (likes) their memory.
 *
 * Same self-like guard as [SendPhotoLikeNotificationUseCase].
 */
class SendGuestbookLikeNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val identityManager: IdentityManager,
    @ApplicationContext private val context: Context,
) {

    /**
     * @param weddingId           The wedding the guestbook post belongs to.
     * @param postId              The guestbook post that was liked.
     * @param postOwnerIdentityId The identity ID of the memory creator.
     */
    suspend operator fun invoke(
        weddingId: String,
        postId: String,
        postOwnerIdentityId: String,
    ): Result<Unit> {
        val reactorId = identityManager.currentIdentityId

        if (reactorId == postOwnerIdentityId || postOwnerIdentityId.isBlank()) {
            return Result.success(Unit)
        }

        val reactorName = GuestSessionManager.getGuestName(context).ifBlank { "Someone" }

        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            weddingId = weddingId,
            recipientId = postOwnerIdentityId,
            senderId = reactorId,
            senderName = reactorName,
            type = NotificationType.GUESTBOOK_LIKE,
            title = "Someone loved your memory 🥂",
            body = "$reactorName loved your guestbook memory.",
            targetId = postId,
            targetScreen = NotificationTargetScreen.GUESTBOOK,
            createdAt = System.currentTimeMillis(),
        )
        return notificationRepository.sendNotification(weddingId, notification)
    }
}
