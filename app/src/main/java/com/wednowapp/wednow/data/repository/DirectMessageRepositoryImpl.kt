package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.DirectMessageFirestoreService
import com.wednowapp.wednow.domain.model.DirectMessage
import com.wednowapp.wednow.domain.repository.DirectMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectMessageRepositoryImpl @Inject constructor(
    private val service: DirectMessageFirestoreService
) : DirectMessageRepository {

    override fun observeMessages(weddingId: String, channelId: String): Flow<List<DirectMessage>> =
        service.observeMessages(weddingId, channelId)

    override suspend fun sendMessage(
        weddingId: String,
        channelId: String,
        message: DirectMessage
    ): Result<Unit> = service.sendMessage(weddingId, channelId, message)
}
