package com.wednowapp.wednow.domain.repository

import com.wednowapp.wednow.domain.model.Broadcast
import kotlinx.coroutines.flow.Flow

interface BroadcastRepository {
    fun observeBroadcasts(weddingId: String): Flow<List<Broadcast>>
    suspend fun sendBroadcast(weddingId: String, broadcast: Broadcast): Result<Unit>
}
