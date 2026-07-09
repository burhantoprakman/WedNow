package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.Broadcast
import com.maritsa.app.domain.repository.BroadcastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBroadcastsUseCase @Inject constructor(
    private val repository: BroadcastRepository
) {
    operator fun invoke(weddingId: String): Flow<List<Broadcast>> =
        repository.observeBroadcasts(weddingId)
}
