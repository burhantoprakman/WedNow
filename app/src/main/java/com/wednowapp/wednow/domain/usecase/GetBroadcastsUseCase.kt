package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.Broadcast
import com.wednowapp.wednow.domain.repository.BroadcastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBroadcastsUseCase @Inject constructor(
    private val repository: BroadcastRepository
) {
    operator fun invoke(weddingId: String): Flow<List<Broadcast>> =
        repository.observeBroadcasts(weddingId)
}
