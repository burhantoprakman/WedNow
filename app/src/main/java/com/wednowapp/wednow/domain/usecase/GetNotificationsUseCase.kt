package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(weddingId: String): Flow<List<AppNotification>> =
        repository.observeNotifications(weddingId)
}
