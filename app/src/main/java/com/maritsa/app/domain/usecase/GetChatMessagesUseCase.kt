package com.maritsa.app.domain.usecase

import com.maritsa.app.domain.model.ChatMessage
import com.maritsa.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(weddingId: String): Flow<List<ChatMessage>> =
        repository.observeMessages(weddingId)
}
