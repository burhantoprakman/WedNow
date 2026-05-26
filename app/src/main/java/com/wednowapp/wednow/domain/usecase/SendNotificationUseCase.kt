package com.wednowapp.wednow.domain.usecase

import android.content.Context
import com.wednowapp.wednow.core.session.GuestSessionManager
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.model.NotificationType
import com.wednowapp.wednow.domain.repository.GuestRepository
import com.wednowapp.wednow.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

class SendNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val guestRepository: GuestRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        weddingId: String,
        title: String,
        body: String,
        type: String = NotificationType.ANNOUNCEMENT
    ): Result<Unit> {
        val guestId = GuestSessionManager.getGuestId(context)
        val guest = guestRepository.getGuestById(weddingId, guestId).firstOrNull()
        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            body = body.trim(),
            type = type,
            timestamp = System.currentTimeMillis(),
            sentBy = guestId,
            sentByName = guest?.name.orEmpty()
        )
        return notificationRepository.sendNotification(weddingId, notification)
    }
}
