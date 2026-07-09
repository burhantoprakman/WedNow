package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.DirectMessage
import com.maritsa.app.domain.repository.DirectMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDirectMessagesUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(weddingId: String, channelId: String): Flow<List<DirectMessage>> =
        repository.observeMessages(weddingId, channelId)
}
