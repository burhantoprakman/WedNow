package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.DirectMessage
import kotlinx.coroutines.flow.Flow

interface DirectMessageRepository {
    fun observeMessages(weddingId: String, channelId: String): Flow<List<DirectMessage>>
    suspend fun sendMessage(
        weddingId: String,
        channelId: String,
        message: DirectMessage
    ): Result<Unit>
}
