package com.wednowapp.wednow.domain.usecase

import com.wednowapp.wednow.domain.model.ChatMessage
import com.wednowapp.wednow.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(weddingId: String): Flow<List<ChatMessage>> =
        repository.observeMessages(weddingId)
}
