package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.NotificationFirestoreService
import com.wednowapp.wednow.domain.model.AppNotification
import com.wednowapp.wednow.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val service: NotificationFirestoreService
) : NotificationRepository {

    override fun observeNotifications(weddingId: String): Flow<List<AppNotification>> =
        service.observeNotifications(weddingId)

    override suspend fun sendNotification(weddingId: String, notification: AppNotification): Result<Unit> =
        service.sendNotification(weddingId, notification)
}
