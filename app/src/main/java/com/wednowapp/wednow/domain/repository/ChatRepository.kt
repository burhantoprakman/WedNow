package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(weddingId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(weddingId: String, message: ChatMessage): Result<Unit>
}
