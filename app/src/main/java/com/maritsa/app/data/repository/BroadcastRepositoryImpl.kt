package com.maritsa.app.data.repository

import com.maritsa.app.data.remote.BroadcastFirestoreService
import com.maritsa.app.domain.model.Broadcast
import com.maritsa.app.domain.repository.BroadcastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BroadcastRepositoryImpl @Inject constructor(
    private val service: BroadcastFirestoreService
) : BroadcastRepository {

    override fun observeBroadcasts(weddingId: String): Flow<List<Broadcast>> =
        service.observeBroadcasts(weddingId)

    override suspend fun sendBroadcast(weddingId: String, broadcast: Broadcast): Result<Unit> =
        service.sendBroadcast(weddingId, broadcast)
}
