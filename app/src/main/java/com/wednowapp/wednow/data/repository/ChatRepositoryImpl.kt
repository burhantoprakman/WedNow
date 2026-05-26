package com.wednowapp.wednow.data.repository

import com.wednowapp.wednow.data.remote.ChatFirestoreService
import com.wednowapp.wednow.domain.model.ChatMessage
import com.wednowapp.wednow.domain.repository.ChatRepository
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
