package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.DirectMessage
import kotlinx.coroutines.flow.Flow

interface DirectMessageRepository {
    fun observeMessages(weddingId: String, channelId: String): Flow<List<DirectMessage>>
    suspend fun sendMessage(weddingId: String, channelId: String, message: DirectMessage): Result<Unit>
}
