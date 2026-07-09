package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.NotificationFirestoreService
import com.maritsa.app.domain.model.AppNotification
import com.maritsa.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val service: NotificationFirestoreService,
) : NotificationRepository {

    override fun observeNotifications(
        weddingId: String,
        recipientId: String,
    ): Flow<List<AppNotification>> =
        service.observeNotifications(weddingId, recipientId)

    override fun observeUnreadCount(
        weddingId: String,
        recipientId: String,
    ): Flow<Int> = combine(
        service.observeNotifications(weddingId, recipientId),
        service.observeReadIds(weddingId, recipientId),
    ) { notifications, readIds ->
        notifications.count { it.id !in readIds }
    }

    override suspend fun sendNotification(
        weddingId: String,
        notification: AppNotification,
    ): Result<Unit> = service.sendNotification(weddingId, notification)

    override suspend fun markAsRead(
        weddingId: String,
        notificationId: String,
        recipientId: String,
    ): Result<Unit> = service.markAsRead(weddingId, notificationId, recipientId)

    override suspend fun markAllAsRead(
        weddingId: String,
        notificationIds: List<String>,
        recipientId: String,
    ): Result<Unit> = service.markAllAsRead(weddingId, notificationIds, recipientId)
}
