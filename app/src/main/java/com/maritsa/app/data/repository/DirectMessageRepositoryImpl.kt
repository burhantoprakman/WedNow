package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.DirectMessageFirestoreService
import com.maritsa.app.domain.model.DirectMessage
import com.maritsa.app.domain.repository.DirectMessageRepository
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
