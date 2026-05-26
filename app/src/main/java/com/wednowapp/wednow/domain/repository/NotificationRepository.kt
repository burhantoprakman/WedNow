package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeNotifications(weddingId: String): Flow<List<AppNotification>>
    suspend fun sendNotification(weddingId: String, notification: AppNotification): Result<Unit>
}
