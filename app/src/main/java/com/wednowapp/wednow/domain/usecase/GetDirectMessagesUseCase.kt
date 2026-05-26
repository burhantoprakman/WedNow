package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.DirectMessage
import com.wednowapp.wednow.domain.repository.DirectMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDirectMessagesUseCase @Inject constructor(
    private val repository: DirectMessageRepository
) {
    operator fun invoke(weddingId: String, channelId: String): Flow<List<DirectMessage>> =
        repository.observeMessages(weddingId, channelId)
}
