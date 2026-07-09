package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.ChatFirestoreService
import com.maritsa.app.domain.model.ChatMessage
import com.maritsa.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val service: ChatFirestoreService
) : ChatRepository {

    override fun observeMessages(weddingId: String): Flow<List<ChatMessage>> =
        service.observeMessages(weddingId)

    override suspend fun sendMessage(weddingId: String, message: ChatMessage): Result<Unit> =
        service.sendMessage(weddingId, message)
}
