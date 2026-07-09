package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.AppNotification
import com.maritsa.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    /**
     * Returns a real-time stream of notifications relevant to [recipientId]:
     * broadcast notifications (recipientId = "") plus personal ones.
     */
    operator fun invoke(
        weddingId: String,
        recipientId: String,
    ): Flow<List<AppNotification>> =
        repository.observeNotifications(weddingId, recipientId)
}
