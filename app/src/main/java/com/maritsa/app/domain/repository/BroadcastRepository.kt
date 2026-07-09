package com.maritsa.app.domain.repository

import com.maritsa.app.domain.model.Broadcast
import kotlinx.coroutines.flow.Flow

interface BroadcastRepository {
    fun observeBroadcasts(weddingId: String): Flow<List<Broadcast>>
    suspend fun sendBroadcast(weddingId: String, broadcast: Broadcast): Result<Unit>
}
